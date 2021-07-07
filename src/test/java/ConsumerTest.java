import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Test;

import java.util.List;

/**
 * 接收消息
 *
 * @Author ext.caojinsong
 * @Date 7/7/2021
 */
public class ConsumerTest {

    /**
     * 最常用得就是： DefaultMQPushConsumer
     *
     * 1. 同样 groupName 的 Consumer 作为一组，只消费 message 一次。
     *      如果不传 默认为 DEFAULT_CONSUMER ，但是抛异常。
     *
     */
    @Test
    public void receive() throws MQClientException, InterruptedException {
        // 实例化消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("please_rename_unique_group_name");

        // 设置NameServer的地址
        consumer.setNamesrvAddr("localhost:9876");

        // 订阅一个或者多个Topic，以及Tag来过滤需要消费的消息
        consumer.subscribe("TopicTest", "*");

        // 注册回调实现类来处理从broker拉取回来的消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

                //打印
                printMsgs(msgs);

                // 标记该消息已经被成功消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        // 启动消费者实例
        consumer.start();

        System.out.printf("Consumer Started.%n");

        Thread.sleep(500000);
    }

    /**
     * 打印消息
     */
    public void printMsgs(List<MessageExt> msgs) {
        System.out.printf("%s 收到消息 【%s】 条 \n ", Thread.currentThread().getName(), msgs.size());
        for (MessageExt msg : msgs) {
            System.out.printf("\t内容：【%s】，消息详情：【%s】 \n\n ", new String(msg.getBody()), msg);
        }
    }
}
