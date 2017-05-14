package cn.net.communion;

public class App {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        System.out.println(new GeoHashHelper().around(44.9999, 116.3967));
        System.out.println(new GeoHashHelper().around(45.0001, 116.3967));
        System.out.println(DistanceHepler.distance(44.9999, 116.3967, 45.0001, 116.3967));
        System.out.println("waste time: " + (System.currentTimeMillis() - start));
        // long start = System.currentTimeMillis();
        // System.out.println(new GeoHashHelper().encode(44.9999, 116.3967));
        // System.out.println(new GeoHashHelper().encode(45.0001, 116.3967));
        // System.out.println("waste time: " + (System.currentTimeMillis() - start));
    }

}
