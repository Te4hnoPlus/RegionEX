package plus.region.adv;

import java.util.ArrayList;


public class AmRegion {
    //null -> is empty, size = 0 -> is full
    private static final int[][][] FULL_0  = new int[0][0][0];
    private static final int[][]   FULL_1 = new int[0][0];
    private static final int[]     FULL_2  = new int[0];
    private static int             FULL_3  = 0xFFFFFFFF;

    private Section[] sections = new Section[16];
    private int sectionsMask = 15, size = 0;

    private static int sectHash(int x, int y, int z){
        return ((x * 13) + z) * 13 + y;
    }


    private static boolean hasBlock(int[] data, /*size 16, by blocks in 8 x 8 x 8*/ int x, int y, int z){
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


    private static void fillFull(int[][][] data){
        for (int[][] datum : data) fillFull(datum);
    }


    private static void fillFull(int[][] data){
        for (int[] datum : data) fillFull(datum);
    }


    private static void fillFull(int[] data){
        for (int i = 0, s = data.length; i < s; i++) data[i] = FULL_3;
    }


    //32 x 32 x 32
    private static final class Section{
        private final int x, z;
        private final byte y;
        //8x8x8
        private int[][][] sections = null;
        private Section next;

        private Section(int x, int y, int z) {
            this.x = x;
            this.y = (byte) y;
            this.z = z;
        }


        //block x, y, z
        private int[] sectionBy(int x, int y, int z){
            int[][][] sections;
            if((sections = this.sections) == null)return null;
            if(sections.length == 0)return FULL_2;

            int[][] sub;
            if((sub = sections[((x & 31) >> 3) << 2 | ((z & 31) >> 3)]) == null)return null;
            if(sub.length == 0)return FULL_2;
            return sub[(y & 31) >> 3];
        }


        private void setSection(int[] data, int x, int y, int z){
            int[][][] sections;
            if((sections = this.sections) == null)this.sections = sections = new int[16][][];
            else if(sections.length == 0) {
                sections = this.sections = new int[16][][];
                for (int i = 0; i < 16; i++) sections[i] = FULL_1;
            }

            x = (x & 31) >> 3;
            z = (z & 31) >> 3;
            y = (y & 31) >> 3;
            int[][] sub = sections[x << 2 | z];
            if(sub == null){
                sub = sections[x << 2 | z] = new int[16][];
            } else {
                if(sub.length == 0){
                    sub = sections[x << 2 | z] = new int[16][];
                    for (int i = 0; i < 16; i++)
                        sub[i] = FULL_2;
                }
            }
            sub[y] = data;
        }


        private boolean isFull(){
            return sections != null && sections.length == 0;
        }


        private boolean isEmpty(){
            return sections == null;
        }


        public void trim(){
            if(sections == null || sections.length == 0)return;
            boolean full = true;
            boolean empty = true;
            for (int as=0; as < 16; as++){
                int[][] section = sections[as];
                if(section == null || section.length == 0)continue;

                boolean sectFull = true;
                boolean sectEmpty = true;

                for (int bs=0; bs < 16; bs++){

                    int[] block = section[bs];
                    if(block == null)continue;

                    if(!isFullInts(block)){
                        sectFull = false;

                        if(!isEmptyInts(block)){
                            sectEmpty = false;
                        } else {
                            section[bs] = null;
                        }
                    } else {
                        section[bs] = FULL_2;
                    }
                }
                if(sectFull){
                    sections[as] = FULL_1;
                    empty = false;
                } else {
                    full = false;
                    if(sectEmpty){
                        sections[as] = null;
                    } else {
                        empty = false;
                    }
                }
            }
            if(full){
                sections = FULL_0;
            } else if(empty){
                sections = null;
            }
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
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
        for(int i = 0; i < sections.length; i++){
            Section sect = sections[i];

            if(sect == null)continue;
            sect.trim();

            if(sect.isEmpty()){
                sections[i] = sect.next;
                --size;
            }

            while (sect.next != null) {
                sect = sect.next;
                sect.trim();
            }
        }
    }


    private void reHashIfNeed(){
        if(size < (sectionsMask + 1) * 2)return;

        final Section[] prev = this.sections;
        final int curMask = this.sectionsMask = (this.sectionsMask << 1) | 1;

        final Section[] curSections = this.sections = new Section[prev.length << 1];

        ArrayList<Section> all = new ArrayList<>(size);
        for(Section sect : prev){
            if(sect == null)continue;
            all.add(sect);

            while (sect.next != null)
                all.add(sect = sect.next);
        }
        for (Section sect : all) {
            int hash = sectHash(sect.x, sect.y, sect.z) & curMask;
            sect.next = null;

            Section next = curSections[hash];
            if(next == null){
                curSections[hash] = sect;
            } else {
                while (next.next != null) {
                    next = next.next;
                }
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
            if(sect.equals(x, (byte) y, z))return sect;

            if((next = sect.next) == null){
                ++size;
                Section result = sect.next = new Section(x, y, z);

                reHashIfNeed();

                return result;
            }
            sect = next;
        }
    }


    public boolean hasBlock(int x, int y, int z){
        final Section sect;
        if((sect = getSection(x, y, z)) == null) return false;

        final int[] section;
        if((section = sect.sectionBy(x, y, z)) == null) return false;
        if(section.length == 0) return true;

        return hasBlock(section, x, y, z);
    }


    public void setBlock(int x, int y, int z) {
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


    public void remBlock(int x, int y, int z) {
        final Section sect;
        if ((sect = getSection(x, y, z)) == null) return;

        int[] section;

        if ((section = sect.sectionBy(x, y, z)) == null) return;

        if (section.length == 0) {
            section = new int[16];
            fillFull(section);
            sect.setSection(section, x, y, z);
        }

        remBlock(section, x, y, z);
    }
}