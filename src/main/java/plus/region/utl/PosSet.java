package plus.region.utl;

import java.util.Iterator;


public class PosSet {
    //Null -> is empty, size = 0 -> is full. This saves a lot of memory for continuous sequences of positions.
    //Used instead of int[]{{{0xFFFFFFFF, .. 0xFFFFFFFF}, {0xFFFFFFFF, ..}, ..}, {{0xFFFFFFFF, ..}, ..} ..}
    private static final int[][][] FULL_0  = new int[0][0][0];
    //Used instead of int[]{{0xFFFFFFFF, .. 0xFFFFFFFF}, {0xFFFFFFFF, .. 0xFFFFFFFF}, ..}
    private static final int[][]   FULL_1  = new int[0][0];
    //Used instead of int[]{0xFFFFFFFF, 0xFFFFFFFF, .. 0xFFFFFFFF}
    private static final int[]     FULL_2  = new int[0];
    private static final int       FULL_3  = 0xFFFFFFFF;

    private Section[] sections = new Section[16];
    private int sectionsMask = 15, size = 0;

    private static int sectHash(int x, int y, int z){
        return ((x * 13) + z) * 13 + y;
    }


    //Cursor: (x & 7) << 1 | (y & 1), Mask: 1 << ((y & 30) << 2 | (z & 7))
    private static boolean containsBlock(int[] data, /*size 16, by blocks in 8 x 8 x 8*/ int x, int y, int z){
        final int mask;
        return ((data[((x & 7) << 1) | (y & 1)]) & (mask = 1 << ((y & 30) << 2 | (z & 7)))) == mask;
    }


    private static void setBlock(int[] data, /*size 16, by blocks in 8 x 8 x 8*/ int x, int y, int z){
        data[((x & 7) << 1) | (y & 1)] |= (1 << ((y & 30) << 2 | (z & 7)));
    }


    private static void remBlock(int[] data, /*size 16, by blocks in 8 x 8 x 8*/ int x, int y, int z){
        data[((x & 7) << 1) | (y & 1)] &= ~(1 << ((y & 30) << 2 | (z & 7)));
    }


    private static boolean isFullInts(int[] data){
        for (int i : data) if (i != FULL_3) return false;
        return true;
    }


    private static boolean isEmptyInts(int[] data){
        for (int i : data) if (i != 0) return false;
        return true;
    }


    private static void fillFull(int[] data){
        for (int i = 0, size = data.length; i < size; i++) data[i] = FULL_3;
    }


    //Region section of 32 x 32 x 32
    private static final class Section{
        private final int x, z;
        private final byte y;
        //Sections blocks of size 8 x 8 x 8
        private int[][][] sections = null;
        //Next section with equal hash
        private Section next;

        //Local x, y, z
        private Section(final int x, final int y, final int z) {
            this.x = x;
            this.y = (byte) y;
            this.z = z;
        }


        //block x, y, z
        private int[] sectionBy(final int x, final int y, final int z){
            final int[][][] sections;
            if((sections = this.sections) == null) return null;
            if(sections.length == 0) return FULL_2;

            final int[][] sub;
            if((sub = sections[((x & 31) >> 3) << 2 | ((z & 31) >> 3)]) == null) return null;
            if(sub.length == 0) return FULL_2;
            return sub[(y & 31) >> 3];
        }


        //block x, y, z
        private void setSection(int[] data, int x, int y, int z){
            int[][][] sections;
            if((sections = this.sections) == null) this.sections = sections = new int[16][][];
            else if(sections.length == 0) {
                sections = this.sections = new int[16][][];
                for (int i = 0; i < 16; i++) sections[i] = FULL_1;
            }

            int[][]         sub = sections[(x = (x & 31) >> 3) << 2 | (z = (z & 31) >> 3)];
            if(sub == null) sub = sections[x << 2 | z] = new int[16][];

            else {
                //Uncompress is full
                if(sub.length == 0){
                    sub = sections[x << 2 | z] = new int[16][];

                    for (int i = 0; i < 16; i++) sub[i] = FULL_2;
                }
            }
            sub[(y & 31) >> 3] = data;
        }


        private boolean isFull(){
            return sections != null && sections.length == 0;
        }


        private boolean isEmpty(){
            return sections == null;
        }


        public void trim(){
            if(sections == null || sections.length == 0) return;
            boolean full = true, empty = true;

            for (int as = 0; as < 16; as++){
                final int[][] section = sections[as];
                if(section == null || section.length == 0) continue;

                boolean sectFull = true, sectEmpty = true;

                for (int bs = 0; bs < 16; bs++){
                    final int[] block;
                    if((block = section[bs]) == null) continue;

                    if(!isFullInts(block)){
                        sectFull = false;

                        if(!isEmptyInts(block)) sectEmpty = false;
                        else                    section[bs] = null;

                    } else
                        section[bs] = FULL_2;
                }

                if(sectFull){
                    sections[as] = FULL_1;
                    empty = false;
                } else {
                    full = false;

                    if(sectEmpty) sections[as] = null;
                    else          empty = false;
                }
            }

            if(full)       sections = FULL_0;
            else if(empty) sections = null;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Section section = (Section) o;
            return x == section.x && z == section.z && y == section.y;
        }


        public boolean equals(int x, byte y, int z){
            return this.x == x && this.y == y && this.z == z;
        }


        @Override
        public int hashCode() {
            return sectHash(x, y, z);
        }
    }


    public void trim(){
        for(int i = 0, size = sections.length; i < size; i++){
            Section sect;

            if((sect = sections[i]) == null) continue;
            sect.trim();

            if(sect.isEmpty()){
                sections[i] = sect.next;
                --this.size;
            }

            while (sect.next != null) (sect = sect.next).trim();
        }
    }


    private static final class RehashItr implements Iterator<Section>, Iterable<Section> {
        private final Section[] sections;
        private Section next;
        private Section next2;
        private int cursor = 0;

        private RehashItr(Section[] sections) {
            this.sections = sections;
            goToNext();
            calcNext();
        }


        private void calcNext(){
            next2 = next;
            if(next == null)return;
            if((next = next.next) == null){
                goToNext();
            }
        }


        private void goToNext(){
            while (cursor < sections.length){
                Section sect = sections[cursor++];
                if(sect != null){
                    next = sect;
                    return;
                }
            }
            next = null;
        }


        @Override
        public boolean hasNext() {
            return next2 != null;
        }


        @Override
        public Section next() {
            Section result = next2;
            calcNext();
            return result;
        }


        @Override
        public Iterator<Section> iterator() {
            return this;
        }
    }


    protected void reHashIfNeed(){
        if(size < ((sectionsMask + 1) << 1)) return;

        final Section[] prev, curSections = this.sections = new Section[(prev = this.sections).length << 1];

        final int curMask = this.sectionsMask = (this.sectionsMask << 1) | 1;

        for (Section sect : new RehashItr(prev)) {
            int hash = sectHash(sect.x, sect.y, sect.z) & curMask;
            sect.next = null;

            Section next =   curSections[hash];
            if(next == null) curSections[hash] = sect;
            else {
                while (next.next != null) next = next.next;
                next.next = sect;
            }
        }
    }


    //block x, y, z
    private Section getSection(int x, int y, int z){
        Section sect;
        if((sect = sections[sectHash(x >>= 5, y >>= 5, z >>= 5) & sectionsMask]) == null) return null;

        while(sect != null){
            if(sect.equals(x, (byte) y, z)) return sect;
            sect = sect.next;
        }
        return null;
    }


    private Section getOrCreateSection(int x, int y, int z){
        final int hash;
        Section sect = sections[hash = (sectHash(x >>= 5, y >>= 5, z >>= 5) & sectionsMask)];

        if(sect == null) {
            ++size;
            sect = sections[hash] = new Section(x, y, z);

            reHashIfNeed();

            return sect;
        }

        Section next;
        while(true){
            if(sect.equals(x, (byte) y, z)) return sect;

            if((next = sect.next) == null){
                ++size;
                Section result = sect.next = new Section(x, y, z);

                reHashIfNeed();

                return result;
            }
            sect = next;
        }
    }


    public boolean contains(final int x, final int y, final int z){
        final Section sect;
        if((sect = getSection(x, y, z)) == null) return false;

        final int[] section;
        return (section = sect.sectionBy(x, y, z)) != null && (section.length == 0 || containsBlock(section, x, y, z));
    }


    public void set(final int x, final int y, final int z) {
        final Section sect;
        if ((sect = getOrCreateSection(x, y, z)).isFull()) return;

        int[] section;

        if ((section = sect.sectionBy(x, y, z)) == null) {
            section = new int[16];
            sect.setSection(section, x, y, z);
        }
        else if (section.length == 0) return;

        setBlock(section, x, y, z);
    }


    public void remove(final int x, final int y, final int z) {
        final Section sect;
        if ((sect = getSection(x, y, z)) == null) return;

        int[] section;
        if ((section = sect.sectionBy(x, y, z)) == null) return;

        if (section.length == 0) {
            fillFull(section = new int[16]);
            sect.setSection(section, x, y, z);
        }
        remBlock(section, x, y, z);
    }
}