import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.plugin.filter.PlugInFilter;

import java.awt.Color;

public class Histogram_Matcher implements PlugInFilter {

    public int setup(String args, ImagePlus im) {
        return DOES_RGB;
    }

    public double[] arrayCumulativ(int[] arr) {
        double[] resultArray = new double[arr.length];
        resultArray[0] = arr[0];
        for (int i = 1; i < arr.length; i++) {
            resultArray[i] = arr[i] + resultArray[i - 1];
        }
        for (int i = 0; i < resultArray.length; i++) {
            resultArray[i] = resultArray[i] / resultArray[255];
        }
        return resultArray;

    }


    public void run(ImageProcessor ip) {

        int[] redColors = {0, 0, 0, 0, 6, 46, 317, 1679, 3969, 4163, 3159, 2305, 1593, 944, 632, 446, 443, 476, 529, 520, 558, 527, 572,
                597, 575, 586, 669, 576, 606, 644, 624, 642, 600, 587, 528, 539, 515, 484, 520, 501, 475, 447,
                459, 473, 413, 411, 391, 356, 367, 378, 347, 363, 378, 340, 314, 282, 300, 293,
                304, 278, 270, 274, 247, 265, 281, 281, 255, 242, 259, 269, 280,
                291, 290, 277, 290, 338, 309, 342, 343, 373, 383, 433, 454, 556,
                532, 592, 613, 621, 669, 721, 852, 1059, 1119, 1215, 1173, 1145,
                1124, 1117, 1045, 928, 881, 992, 1057, 1130, 1160, 1250, 1355, 1512,
                1341, 1378, 1288, 1256, 1289, 1241, 1206, 1244, 1181, 1180, 1231, 1299,
                1275, 1251, 1225, 1247, 1212, 1178, 1313, 1283, 1303, 1274, 1280, 1367,
                1526, 1609, 1687, 1721, 1712, 1834, 1954, 1965, 1927, 1825, 1835, 1774,
                1932, 1879, 1808, 1836, 1717, 1681, 1593, 1561, 1503, 1535, 1614, 1819,
                2148, 2608, 3006, 3327, 3599, 3809, 3913, 3919, 4137, 4298, 4844, 5525,
                6497, 6942, 7042, 6888, 6504, 6323, 6001, 5932, 6485, 6870, 7474, 7297,
                6625, 6540, 6338, 6059, 5577, 3978, 2003,
                776, 174, 34, 6, 3, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        int[] blueColors = {0,0,0,1,2,8,60,363,1710,3863,4131,3536,3110,2297,1637,
                1481,1366,1355,1314,1301,1349,1231,1285,1143,1182,1113,1044,967,879,
                852,811,726,769,710,641,694,603,768,839,961,1146,1293,1443,1515,1453,
                1444,1337,1310,1190,1131,910,920,960,993,1135,1076,1089,1030,995,850,870,
                873,904,927,952,1035,965,965,889,803,821,856,788,810,854,821,809,748,699,
                779,804,847,792,810,826,766,786,812,828,846,848,892,793,777,882,977,996,
                1012,1035,991,1026,924,710,630,521,504,484,456,417,444,422,372,398,390,427,
                385,385,412,380,422,410,427,392,463,410,468,471,517,560,606,602,707,807,849,
                920,908,1001,1099,1230,1152,1115,1260,1176,1333,1325,1299,1263,1179,1126,1048,
                1033,949,1024,1128,1297,1549,1812,2284,2837,3467,3918,4206,4391,4594,4964,5635,
                6327,7308,7779,7499,7033,6249,5783,5715,5938,6414,6482,6719,6605,6175,6097,6315,
                6033,5292,3627,1693,666,236,68,15,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

        int[] greenColors = {0,0,0,0,0,1,3,76,426,1486,3567,3909,3422,2880,2005,1231,864,808,
                909,968,1014,1017,982,953,962,942,939,878,877,842,793,809,769,678,643,617,605,
                495,535,520,486,454,445,447,447,401,408,403,382,382,384,380,391,404,429,425,
                423,464,579,665,784,877,899,1018,1093,1188,1207,1225,1082,969,897,806,694,673,
                656,699,756,740,867,894,914,911,889,909,949,891,762,784,774,906,846,919,935,
                1056,997,1067,1107,1104,1102,1126,1038,1108,1202,1340,1278,1322,1376,1461,
                1496,1341,1226,1167,1139,1161,1175,1187,1164,1167,1076,1050,913,920,865,864,
                753,737,659,632,603,524,536,533,508,577,673,695,798,869,957,1050,1109,1280,
                1258,1220,1158,1097,1088,1046,1093,1098,1085,998,1058,1000,1100,1068,1174,
                1206,1415,1650,1901,2373,2952,3512,3700,3820,4014,4037,4194,4583,5562,6462,
                7237,7740,7671,7273,6894,6469,6849,7159,7843,7748,7115,7229,7352,7104,6110,
                3734,1452,435,103,31,2,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

        int[] badRed = new int[256];
        int[] badBlue = new int[256];
        int[] badGreen = new int[256];

        int width = ip.getWidth(), height = ip.getHeight(), pixel, r, g, b;
        Color color;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                color = new Color(ip.getPixel(col, row));
                badRed[color.getRed()]++;
                badGreen[color.getGreen()]++;
                badBlue[color.getBlue()]++;
            }
        }
        double[] badRedCumulativ = arrayCumulativ(badRed);
        double[] badGreenCumulativ = arrayCumulativ(badGreen);
        double[] badBlueCumulativ = arrayCumulativ(badBlue);

        double[] goodRedCumulativ = arrayCumulativ(redColors);
        double[] goodGreenCumulativ = arrayCumulativ(greenColors);
        double[] goodBlueCumulativ = arrayCumulativ(blueColors);


        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                color = new Color(ip.getPixel(col, row));
                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();
                int[] colorInt = {search(goodRedCumulativ, 0, goodRedCumulativ.length - 1, badRedCumulativ[r]),
                        search(goodGreenCumulativ, 0, goodGreenCumulativ.length - 1, badGreenCumulativ[g]),
                        search(goodBlueCumulativ, 0, goodBlueCumulativ.length - 1, badBlueCumulativ[b])};
                ip.putPixel(col, row, colorInt);
            }
        }
    }

    public int search(double arr[], int first, int last, double key) {
        if (last >= first) {
            int mid = first + (last - first) / 2;
            if (arr[mid] == key) {
                return mid;
            }
            if (arr[mid] > key) {
                return search(arr, first, mid - 1, key);
            } else {
                return search(arr, mid + 1, last, key);
            }
        }
        return last;
    }
}

