[回目录](../../README.md)
[下一页](install.md)

# 快速入门

## 发送消息

```java
MessageProducerProvider producer = new MessageProducerProvider();
producer.init();

Message message = producer.generateMessage("your subject");
message.setProperty("key", "value");
//发送延迟消息
//message.setDelayTime(15, TimeUnit.MINUTES);
producer.sendMessage(message);
```

## 消费消息

```java
@QmqConsumer(subject = "your subject", consumerGroup = "group")
public void onMessage(Message message){
    //process your message
    String value = message.getStringProperty("key");
}
```

[回目录](../../README.md)
[下一页](install.md)
