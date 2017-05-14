package cn.net.communion;

public class App {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        System.out.println(new GeoHashHelper().around(39.999252, 116.402844));
        System.out.println(new GeoHashHelper().around(39.99345, 116.391112));
        System.out.println(new GeoHashHelper().around(39.987293, 116.387434));
        System.out.println(DistanceHepler.distance(39.999252, 116.402844, 39.99345, 116.391112));
        System.out.println(DistanceHepler.distance(39.99345, 116.391112, 39.987293, 116.387434));
        System.out.println("waste time: " + (System.currentTimeMillis() - start));
    }

}
