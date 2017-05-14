# Geohash
GeoHash是目前比较主流实现位置服务的技术，Geohash算法将经纬度二维数据编码为一个字符串，本质是一个降维的过程，

### 一个栗子
|地点|经纬度|Geohash|
|:--:|:--:|:--:|
|鸟巢|116.402843,39.999375|wx4g8c9v|
|水立方|116.3967,39.99932|wx4g89tk|
|故宫|116.40382,39.918118|wx4g0ffe|

水立方就在鸟巢在附近，距离600米左右，而故宫到鸟巢直线距离9公里左右，体现在Geohash上，鸟巢和水立方的前五位是一样的，而鸟巢和故宫只有前4位是一样的，也就是说Geohash前面相同的越多，两个位置越近，但是反过来说，却不一定正确，这个在后面会详细介绍。

### 原理
将经纬度转换为Geohash大体可以分为三步曲：
- 将纬度(-90, 90)平均分成两个区间(-90, 0)、(0, 90)，如果坐标位置的纬度值在第一区间，则编码是0，否则编码为1。我们用 39.918118 举例，由于39.918118 属于 (0, 90)，所以编码为1，然后我们继续将(0, 90)分成(0, 45)、(45, 90)两个区间，而39.918118 位于(0, 45)，所以编码是0，依次类推，我们进行20次拆分，最后计算39.918118 的编码是 10111000110001011011；经度的处理也是类似，只是经度的范围是(-180, 180)，116.40382的编码是**11010010110001101010**
- 经纬度的编码合并，从0开始，奇数为是纬度，偶数为是经度，得到的编码是**1110011101001000111100000011100111001101**
- 对经纬度合并后的编码，进行base32编码，最终得到**wx4g0ffe**

### 代码实现
> 将经纬度转换为二进制编码
```java
    private void convert(double min, double max, double value, List<Character> list) {
        if (list.size() > (length - 1)) {
            return;
        }
        double mid = (max + min) / 2;
        if (value < mid) {
            list.add('0');
            convert(min, mid, value, list);
        } else {
            list.add('1');
            convert(mid, max, value, list);
        }
    }
```
> 合并经纬度的二进制编码
```java
        List<Character> latList = new ArrayList<Character>();
        List<Character> lngList = new ArrayList<Character>();
        convert(Min_Lat, Max_Lat, lat, latList);
        convert(Min_Lng, Max_Lng, lng, lngList);
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < latList.size(); index++) {
            sb.append(lngList.get(index)).append(latList.get(index));
        }
```
> base32编码
```java
    private final String[] base32Lookup =
            {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "b", "c", "d", "e", "f", "g", "h",
                    "j", "k", "m", "n", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    private String base32Encode(final String str) {
        String unit = "";
        StringBuilder sb = new StringBuilder();
        for (int start = 0; start < str.length(); start = start + 5) {
            unit = str.substring(start, start + 5);
            sb.append(base32Lookup[convertToIndex(unit.split(""))]);
        }
        return sb.toString();
    }
    private int convertToIndex(String str) {
        int length = str.length();
        int result = 0;
        for (int index = 0; index < length; index++) {
            result += str.charAt(index) == '0' ? 0 : 1 << (length - 1 - index);
        }
        return result;
    }
```

### 边界问题
两个位置距离得越近是否意味着Geohash前面相同的越多呢？答案是否定的，两个很近的地点[116.3967,44.9999]和[116.3967,45.0009]的Geohash分别是**wxfzbxvr**和**y84b08j2**，这就是Geohash存在的边界问题，这两个地点虽然很近，但是刚好在分界点45两侧，导致Geohash完全不同，单纯依靠Geohash匹配前缀的方式并不能解决这种问题
![](http://chuantu.biz/t5/86/1494755470x2890186008.jpg)

在一维空间解决不了这个问题，回到二维空间中，将当前Geohash这块区域周围的八块区域的Geohash计算出来
[116.3967,44.9999] 周围8块区域的Geohash
> <b>y84b08j2</b>, wxfzbxvq, wxfzbxvx, wxfzbxvp, y84b08j8, y84b08j0, wxfzbxvw, wxfzbxvn

[116.3967,45.0009] 周围8块区域的Geohash
> y84b08j3, <b>wxfzbxvr</b>, y84b08j8, y84b08j0, y84b08j9, y84b08j1, wxfzbxvx, wxfzbxvp

[116.3967,44.9999]和[116.3967,45.0009]分别出现在各自附近的区域中，周围8个区域的Geohash怎么计算得到呢？很简单，当Geohash长度是8时，对应的每个最小单元
```java
    double latUnit = (Max_Lat - Min_Lat) / (1 << 20);
    double lngUnit = (Max_Lng - Min_Lng) / (1 << 20);
```
这样可以计算出8个分别分布在周围8个区域的地点，根据地点便可以计算出周围8个区域的Geohash
```
[lat + latUnit, lng]
[lat - latUnit, lng]
[lat, lng + lngUnit]
[lat, lng - lngUnit]
[lat + latUnit, lng + lngUnit]
[lat + latUnit, lng - lngUnit]
[lat - latUnit, lng + lngUnit]
[lat - latUnit, lng - lngUnit]
```

### 距离还是距离
打开饿了么这样的应用，除了可以看到附近的商家外，还能清晰看到离每个商家的距离，这个距离的怎么计算出呢？这完全是一个数学问题，把地球看着一个球体，先根据经纬度算出空间坐标，进而算出两点直线距离，最后算出弧长，便是两个位置的距离
```java
    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        double x1 = Math.cos(lat1) * Math.cos(lng1);
        double y1 = Math.cos(lat1) * Math.sin(lng1);
        double z1 = Math.sin(lat1);
        double x2 = Math.cos(lat2) * Math.cos(lng2);
        double y2 = Math.cos(lat2) * Math.sin(lng2);
        double z2 = Math.sin(lat2);
        double lineDistance =
                Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
        double realDistance = EARTH_RADIUS * Math.PI * 2 * Math.asin(0.5 * lineDistance) / 180;
        return realDistance;
    }
```
在实际应用中，先根据Geohash筛选出附近的地点，然后再算出距离附近地点的距离


