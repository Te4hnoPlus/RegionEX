package am;

import plus.region.utl.PosSet;
import java.util.Random;


public class TestAmRegion {

    public static void main(String[] args) {

        PosSet posSet = new PosSet();

        int lim = 150;
        int min = -100;

        for (int x = min; x < lim; x++) {
            for (int y = 0; y < lim; y++) {
                for (int z = min; z < lim; z++) {
                    testPos(posSet, x, y, z);
                }
            }
        }

        posSet.trim();

        Random random = new Random(0);

        for (int x = min; x < lim; x++) {
            for (int y = 0; y < lim; y++) {
                for (int z = min; z < lim; z++) {
                    try {
                        if (!posSet.contains(x, y, z)) {
                            System.out.println(posSet.contains(x, y, z));
                            throw new RuntimeException("ERROR x=" + x + " y=" + y + " z=" + z);
                        } else {
                            if (random.nextBoolean()) {
                                try {
                                    posSet.remove(x, y, z);
                                } catch (Exception e) {
                                    posSet.remove(x, y, z);
                                    throw new RuntimeException("ERROR x=" + x + " y=" + y + " z=" + z, e);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(posSet.contains(x, y, z));
                        throw new RuntimeException("ERROR x=" + x + " y=" + y + " z=" + z, e);
                    }
                }
            }
        }

        random.setSeed(0);

        for (int x = min; x < lim; x++) {
            for (int y = 0; y < lim; y++) {
                for (int z = min; z < lim; z++) {
                    if(random.nextBoolean()) {
                        if(posSet.contains(x, y, z)) {
                            throw new RuntimeException("ERROR x=" + x + " y=" + y + " z=" + z);
                        }
                    }
                }
            }
        }

        System.out.println("TESTED ["+min+","+min+","+min+"] -> ["+lim+","+lim+","+lim+"]");
    }


    private static void testPos(PosSet posSet, int x, int y, int z) {
        try {
            boolean prev = posSet.contains(x, y, z);
            posSet.set(x, y, z);
            boolean cir = posSet.contains(x, y, z);
            if (prev == cir) {
                throw new RuntimeException("ERROR x=" + x + " y=" + y + " z=" + z);
            }
        } catch (Exception e) {
            throw new RuntimeException("ERROR x=" + x + " y=" + y + " z=" + z, e);
        }
    }
}