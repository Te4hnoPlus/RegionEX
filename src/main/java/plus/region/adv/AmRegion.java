package plus.region.adv;

import java.util.ArrayList;


public class AmRegion {
    //null -> is empty, size = 0 -> is full
    private static final int[][][] FULL_0  = new int[0][0][0];
    private static final int[][]   FILL_1  = new int[0][0];
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


    private static boolean checkFullInts(int[] data){
        for (int i : data) if (i != FULL_3) return false;
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
        private int[][][] sections = null; // 16 x 16 x 16
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
            x = (x & 31) >> 3;
            z = (z & 31) >> 3;
            y = (y & 31) >> 3;
            int[][] sub = sections[x << 2 | z];
            if(sub == null)return null;
            if(sub.length == 0)return FULL_2;
            return sub[y];
        }


        private void setSection(int[] data, int x, int y, int z){
            int[][][] sections;
            if((sections = this.sections) == null)this.sections = sections = new int[16][][];
            else if(sections.length == 0) {
                sections = this.sections = new int[16][][];
                for (int i = 0; i < 16; i++) sections[i] = FILL_1;
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
                    for (int i=0;i<16;i++){
                        if(i != y){
                            int[] nums = new int[16];
                            fillFull(nums);
                            sub[i] = nums;
                        }
                    }
                }
            }
            sub[y] = data;
        }


        private boolean isFull(){
            return sections != null && sections.length == 0;
        }


        public void checkFull(){
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

                    if(!checkFullInts(block)){
                        sectFull = false;
                    } else {
                        sectEmpty = false;
                        section[bs] = FULL_2;
                    }
                }
                if(sectFull){
                    sections[as] = FILL_1;
                    empty = false;
                } else {
                    full = false;
                    if(sectEmpty){
                        //sections[as] = null;
                    }
                }
            }
            if(full){
                sections = FULL_0;
            } else if(empty){
                //sections = null;
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


    public void checkFull(){
        for(Section sect : sections){
            if(sect == null)continue;
            sect.checkFull();
            while (sect.next != null) {
                sect = sect.next;
                sect.checkFull();
            }
        }
    }


    private void reHashIfNeed(){
        if(size <= sectionsMask * 2)return;

        Section[] prev = this.sections;
        int prevMask = this.sectionsMask;
        int curMask = this.sectionsMask = (prevMask << 1) | 1;

        Section[] curSections = this.sections = new Section[prev.length << 1];

        ArrayList<Section> all = new ArrayList<>();
        for(Section sect : prev){
            if(sect == null)continue;
            all.add(sect);
            while (sect.next != null) {
                all.add(sect = sect.next);
            }
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
        x >>= 5;
        y >>= 5;
        z >>= 5;

        Section sect = sections[sectHash(x, y, z) & sectionsMask];
        if(sect == null)return null;
        while(sect != null){
            if(sect.equals(x, (byte) y, z))return sect;
            sect = sect.next;
        }
        return null;
    }


    private Section getOrCreateSection(int x, int y, int z){
        x >>= 5;
        y >>= 5;
        z >>= 5;

        final int hash;
        Section sect = sections[hash = (sectHash(x, y, z) & sectionsMask)];
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
        Section sect = getSection(x, y, z);
        if(sect == null) return false;
        if(sect.isFull())return true;

        int[] section = sect.sectionBy(x, y, z);
        if(section == null)    return false;
        if(section.length == 0)return true;

        return hasBlock(section, x, y, z);
    }


    public void setBlock(int x, int y, int z) {
        Section sect = getOrCreateSection(x, y, z);
        if (sect.isFull()) return;

        int[] section;

        if(sect.isFull()){
            section = new int[16];
            sect.setSection(section, x, y, z);
        } else {
            section = sect.sectionBy(x, y, z);
        }

        if (section == null) {
            section = new int[16];
            sect.setSection(section, x, y, z);
        } else if (section.length == 0) {
            return;
        }

        setBlock(section, x, y, z);
    }


    public void remBlock(int x, int y, int z) {
        final Section sect = getSection(x, y, z);
        if (sect == null) return;

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