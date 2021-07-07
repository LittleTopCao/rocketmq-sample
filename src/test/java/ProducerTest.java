import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.CountDownLatch2;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

/**
 * 发送消息
 * <p>
 * 1. 启动 nameServer 时，可以选择是否自动创建 topic。
 * 如果不是自动创建，发送前需要有 topic，否则会报错 org.apache.rocketmq.client.exception.MQClientException: No route info of this topic, XXX
 *
 * @Author ext.caojinsong
 * @Date 7/7/2021
 */
public class ProducerTest {

    /**
     * 1. 同步发送
     * Producer 的 groupName 在事务消息场景下使用，例如当一个 Producer 意外关闭，事务消息的回调会找它同 group 的其他 Producer
     */
    @Test
    public void syncSend() throws MQClientException, UnsupportedEncodingException, RemotingException, InterruptedException, MQBrokerException {
        /*
         * 1. 启动 DefaultMQProducer
         */
        // 实例化消息生产者Producer
        DefaultMQProducer producer = new DefaultMQProducer("please_rename_unique_group_name");
        // 设置NameServer的地址
        producer.setNamesrvAddr("localhost:9876");
        // 启动Producer实例
        producer.start();

        /*
         * 2. 构造并发送消息
         */
        for (int i = 0; i < 100; i++) {
            // 创建消息，并指定Topic，Tag和消息体
            Message msg = new Message("TopicTest", "TagA", ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));

            // 发送消息到一个Broker
            SendResult sendResult = producer.send(msg);

            // 通过sendResult返回消息是否成功送达
            System.out.printf("%s%n", sendResult);
        }

        /*
         * 3. 关闭 DefaultMQProducer
         */
        // 如果不再发送消息，关闭Producer实例。
        producer.shutdown();
    }


    /**
     * 2. 异步发送
     * 在调用 send 方法时 设置回调，立即返回
     */
    @Test
    public void asyncSend() throws MQClientException, UnsupportedEncodingException, RemotingException, InterruptedException {
        // 实例化消息生产者Producer
        DefaultMQProducer producer = new DefaultMQProducer("please_rename_unique_group_name");
        // 设置NameServer的地址
        producer.setNamesrvAddr("localhost:9876");
        // 启动Producer实例
        producer.start();

        producer.setRetryTimesWhenSendAsyncFailed(0);

        int messageCount = 100;

        // 根据消息数量实例化倒计时计算器
        final CountDownLatch2 countDownLatch = new CountDownLatch2(messageCount);

        for (int i = 0; i < messageCount; i++) {

            final int index = i;

            // 创建消息，并指定Topic，Tag和消息体
            Message msg = new Message("TopicTest", "TagA", "OrderID188", "Hello world".getBytes(RemotingHelper.DEFAULT_CHARSET));

            // SendCallback接收异步返回结果的回调
            producer.send(msg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.printf("%-10d OK %s %n", index, sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable e) {
                    System.out.printf("%-10d Exception %s %n", index, e);
                    e.printStackTrace();
                }
            });
        }

        // 等待5s
        countDownLatch.await(5, TimeUnit.SECONDS);

        // 如果不再发送消息，关闭Producer实例。
        producer.shutdown();
    }


    /**
     * 3. 单向发送
     * 调用 sendOneway 发送消息，立即返回，不等待是否发送成功
     */
    @Test
    public void onewaySend() throws MQClientException, UnsupportedEncodingException, RemotingException, InterruptedException {
        // 实例化消息生产者Producer
        DefaultMQProducer producer = new DefaultMQProducer("please_rename_unique_group_name");

        // 设置NameServer的地址
        producer.setNamesrvAddr("localhost:9876");

        // 启动Producer实例
        producer.start();

        for (int i = 0; i < 100; i++) {

            // 创建消息，并指定Topic，Tag和消息体
            Message msg = new Message("TopicTest", "TagA", ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));

            // 发送单向消息，没有任何返回结果
            producer.sendOneway(msg);
        }

        // 如果不再发送消息，关闭Producer实例。
        producer.shutdown();
    }


}
