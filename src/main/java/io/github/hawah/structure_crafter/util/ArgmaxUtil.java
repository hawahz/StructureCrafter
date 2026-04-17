package io.github.hawah.structure_crafter.util;


import java.util.Comparator;
import java.util.List;

public class ArgmaxUtil {

    /**
     * 求 int 数组的 argmax，下标值从 0 开始，返回第一个出现最大值的下标。
     * @param array 输入 int 数组
     * @return 最大值所在的下标
     * @throws IllegalArgumentException 若数组为空或长度为 0
     */
    public static int argmax(int[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("输入数组不能为空且长度必须大于 0");
        }
        int maxIndex = 0;
        int maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * 求 double 数组的 argmax，下标值从 0 开始，返回第一个出现最大值的下标。
     * @param array 输入 double 数组
     * @return 最大值所在的下标
     * @throws IllegalArgumentException 若数组为空或长度为 0
     */
    public static int argmax(double[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("输入数组不能为空且长度必须大于 0");
        }
        int maxIndex = 0;
        double maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * 求 List<T> 集合的 argmax，其中 T 必须实现 Comparable 接口。
     * 返回第一个出现最大值的下标。
     * @param <T> 数据类型，必须实现 Comparable<T>
     * @param list 输入 List 集合
     * @return 最大值所在的下标
     * @throws IllegalArgumentException 若集合为空或长度为 0
     */
    public static <T extends Comparable<T>> int argmax(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("输入集合不能为空且长度必须大于 0");
        }
        int maxIndex = 0;
        T maxValue = list.getFirst();
        for (int i = 1; i < list.size(); i++) {
            T current = list.get(i);
            if (current.compareTo(maxValue) > 0) {
                maxValue = current;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * 求 List<T> 集合的 argmax，通过自定义 Comparator 进行比较。
     * 返回第一个出现最大值的下标。
     * @param <T> 数据类型
     * @param list 输入 List 集合
     * @param comp 自定义比较器
     * @return 最大值所在的下标
     * @throws IllegalArgumentException 若集合为空或长度为 0
     */
    public static <T> int argmax(List<T> list, Comparator<T> comp) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("输入集合不能为空且长度必须大于 0");
        }
        int maxIndex = 0;
        T maxValue = list.getFirst();
        for (int i = 1; i < list.size(); i++) {
            T current = list.get(i);
            if (comp.compare(current, maxValue) > 0) {
                maxValue = current;
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}

