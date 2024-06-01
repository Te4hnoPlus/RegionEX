package am;

import plus.region.adv.AmRegion;
import java.util.Random;


public class TestAmRegion {

    public static void main(String[] args) {

        AmRegion amRegion = new AmRegion();

        int lim = 150;
        int min = -100;

        for (int x = min; x < lim; x++) {
            for (int y = 0; y < lim; y++) {
                for (int z = min; z < lim; z++) {
                    testPos(amRegion, x, y, z);
                }
            }
        }

        amRegion.trim();

        Random random = new Random(0);

        for (int x = min; x < lim; x++) {
            for (int y = 0; y < lim; y++) {
                for (int z = min; z < lim; z++) {
                    try {
                        if (!amRegion.hasBlock(x, y, z)) {
                            System.out.println(amRegion.hasBlock(x, y, z));
                            throw new RuntimeException("ERROR x=" + x + " y=" + y + " z=" + z);
                        } else {
                            if (random.nextBoolean()) {
                                try {
                                    amRegion.remBlock(x, y, z);
                                } catch (Exception e) {
                                    amRegion.remBlock(x, y, z);
                                    throw new RuntimeException("ERROR x=" + x + " y=" + y + " z=" + z, e);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(amRegion.hasBlock(x, y, z));
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
                        if(amRegion.hasBlock(x, y, z)) {
                            throw new RuntimeException("ERROR x=" + x + " y=" + y + " z=" + z);
                        }
                    }
                }
            }
        }

        System.out.println("TESTED ["+min+","+min+","+min+"] -> ["+lim+","+lim+","+lim+"]");
    }


    private static void testPos(AmRegion amRegion, int x, int y, int z) {
        try {
            boolean prev = amRegion.hasBlock(x, y, z);
            amRegion.setBlock(x, y, z);
            boolean cir = amRegion.hasBlock(x, y, z);
            if (prev == cir) {
                throw new RuntimeException("ERROR x=" + x + " y=" + y + " z=" + z);
            }
        } catch (Exception e) {
            throw new RuntimeException("ERROR x=" + x + " y=" + y + " z=" + z, e);
        }
    }
}