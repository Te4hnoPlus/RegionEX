import plus.region.Region;

public class TestRegMath {

    public static void main(String[] args) {

        Region region = new Region(0, 0, 0, 10, 10, 10);
        Region second = new Region(1, 1, 1, 9, 9, 9);
        Region third = new Region(12, 12, 12, 8, 8, 8);

        Region fourth = new Region(13, 13, 13, 17, 17, 17);

        //true
        System.out.println(region.intersects(second));
        System.out.println(second.intersects(region));
        System.out.println(region.intersects(third));

        //false
        System.out.println(region.intersects(fourth));
        System.out.println(second.intersects(fourth));
        System.out.println(third.intersects(fourth));
    }
}
