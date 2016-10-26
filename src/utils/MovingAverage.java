package utils;
//����ٶ�ƽ��ֵ �Ӵ�
import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage {
    private static float filterSum = 0;
    private static float filterResult = 0;
    private final static Queue<Float> maWindow = new LinkedList<>();
//����ٶ�ƽ��ֵ acc�����ٶȵ�ֵ length�����ڳ��ȣ���ƽ��ʱ/length
    public static float movingAverage(float acc, int length) {
        filterSum += acc;
        maWindow.add(acc);
        
        if(maWindow.size() > length) {
            float head = maWindow.remove();
            filterSum -= head;
        }
        //��length�����ٶȵ�ƽ��ֵ
        if(! maWindow.isEmpty()) {
            filterResult = filterSum / maWindow.size();
        }
        return filterResult;
    }
}
