# 1. 概念和特性

## 1.1 概念(Concept)

1. Producer 消息生产者。提供多种发送方式，同步发送、异步发送、顺序发送、单向发送。同步和异步方式均需要Broker返回确认信息，单向发送不需要。
2. Broker Server 代理服务器。消息中转角色，负责存储消息、转发消息。
3. Consumer 消息消费者。负责消费消息，一般是后台系统负责异步消费。一个消息消费者会从Broker服务器拉取消息、并将其提供给应用程序。
4. Topic 主题。表示一类消息的集合，每个主题包含若干条消息，每条消息只能属于一个主题，是RocketMQ进行消息订阅的基本单位。
5. Name Server 名字服务。生产者或消费者能够通过名字服务查找各主题相应的Broker IP列表。
6. Pull Consumer 拉取式消费。Consumer 消费的一种类型，应用通常主动调用Consumer的拉消息方法从Broker服务器拉消息、主动权由应用控制。
7. Push Consumer 推动式消费。Consumer 消费的一种类型，该模式下Broker收到数据后会主动推送给消费端，该消费模式一般实时性较高。
8. Producer Group 生产者组。同一类Producer的集合，这类Producer发送同一类消息且发送逻辑一致。如果发送的是事务消息且原始生产者在发送之后崩溃，则Broker服务器会联系同一生产者组的其他生产者实例以提交或回溯消费。
9. Consumer Group 消费者组。同一类Consumer的集合，这类Consumer通常消费同一类消息且消费逻辑一致。消费者组使得在消息消费方面，实现负载均衡和容错的目标变得非常容易。要注意的是，消费者组的消费者实例必须订阅完全相同的Topic。RocketMQ 支持两种消息模式：集群消费（Clustering）和广播消费（Broadcasting）。
10. Clustering 集群消费。集群消费模式下,相同Consumer Group的每个Consumer实例平均分摊消息。
11. Broadcasting 广播消费。广播消费模式下，相同Consumer Group的每个Consumer实例都接收全量的消息。
12. Normal Ordered Message 普通顺序消息。普通顺序消费模式下，消费者通过同一个消费队列收到的消息是有顺序的，不同消息队列收到的消息则可能是无顺序的。
13. Strictly Ordered Message 严格顺序消息。严格顺序消息模式下，消费者收到的所有消息均是有顺序的。
14. Message 消息。消息系统所传输信息的物理载体，生产和消费数据的最小单位，每条消息必须属于一个主题。
15. Tag 标签。为消息设置的标志，用于同一主题下区分不同类型的消息。来自同一业务单元的消息，可以根据不同业务目的在同一主题下设置不同标签。

## 1.2 特性(Features)

1. 订阅与发布
   消息的发布是指某个生产者向某个topic发送消息；
   消息的订阅是指某个消费者关注了某个topic中带有某些tag的消息，进而从该topic消费数据。
2. 消息顺序
   消息有序指的是一类消息消费时，能按照发送的顺序来消费。顺序消息分为全局顺序消息与分区顺序消息
   全局顺序 对于指定的一个 Topic，所有消息按照严格的先入先出（FIFO）的顺序进行发布和消费。
   分区顺序 对于指定的一个 Topic，所有消息根据 sharding key 进行区块分区。 同一个分区内的消息按照严格的 FIFO 顺序进行发布和消费。 Sharding key 是顺序消息中用来区分不同分区的关键字段，和普通消息的 Key 是完全不同的概念。
3. 消息过滤
   RocketMQ 的消费者可以根据Tag进行消息过滤，也支持自定义属性过滤。
   消息过滤目前是在Broker端实现的，优点是减少了对于Consumer无用消息的网络传输，缺点是增加了Broker的负担、而且实现相对复杂。
4. 消息可靠性
   RocketMQ支持消息的高可靠，影响消息可靠性的几种情况：
      1. Broker非正常关闭
      2. Broker异常Crash
      3. OS Crash
      4. 机器掉电，但是能立即恢复供电情况
      5. 机器无法开机（可能是cpu、主板、内存等关键设备损坏）
      6. 磁盘设备损坏
   1\2\3\4 四种情况都属于硬件资源可立即恢复的情况，能保证消息不丢失（异步刷盘可能丢失少量消息）
   5、6 属于单点故障，且无法恢复，一旦发生，在此单点上的消息全部丢失。通过异步复制可以保证只丢失少量消息。如果加上同步双写技术可以保证消息不丢。
5. 至少一次
   至少一次(At least Once)指每个消息必须投递一次。Consumer先Pull消息到本地，消费完成后，才向服务器返回ack，如果没有消费一定不会ack消息，所以RocketMQ可以很好的支持此特性。
6. 回溯消费
   回溯消费是指Consumer已经消费成功的消息，由于业务上需求需要重新消费，要支持此功能，Broker在向Consumer投递成功消息后，消息仍然需要保留。
   并且重新消费一般是按照时间维度，例如由于Consumer系统故障，恢复后需要重新消费1小时前的数据，那么Broker要提供一种机制，可以按照时间维度来回退消费进度。RocketMQ支持按照时间回溯消费，时间维度精确到毫秒。
7. 事务消息
   RocketMQ事务消息（Transactional Message）是指应用本地事务和发送消息操作可以被定义到全局事务中，要么同时成功，要么同时失败。
   RocketMQ的事务消息提供类似 X/Open XA 的分布事务功能，通过事务消息能达到分布式事务的最终一致。
8. 定时消息
   定时消息（延迟队列）是指消息发送到broker后，不会立即被消费，等待特定时间投递给真正的topic。
9. 消息重试
   Consumer消费消息失败后，要提供一种重试机制，令消息再消费一次。Consumer消费消息失败通常可以认为有以下几种情况：
10. 消息重投
    生产者在发送消息时，同步消息失败会重投，异步消息有重试，oneway没有任何保证。
    消息重投保证消息尽可能发送成功、不丢失，但可能会造成消息重复，消息重复在RocketMQ中是无法避免的问题。
    1. retryTimesWhenSendFailed:同步发送失败重投次数，默认为2，因此生产者会最多尝试发送retryTimesWhenSendFailed + 1次。不会选择上次失败的broker，尝试向其他broker发送，最大程度保证消息不丢。超过重投次数，抛出异常，由客户端保证消息不丢。当出现RemotingException、MQClientException和部分MQBrokerException时会重投。
    2. retryTimesWhenSendAsyncFailed:异步发送失败重试次数，异步重试不会选择其他broker，仅在同一个broker上做重试，不保证消息不丢。
    3. retryAnotherBrokerWhenNotStoreOK:消息刷盘（主或备）超时或slave不可用（返回状态非SEND_OK），是否尝试发送到其他broker，默认false。十分重要消息可以开启。
11. 流量控制
   生产者流控，因为broker处理能力达到瓶颈；生产者流控，不会尝试消息重投。
      1. commitLog文件被锁时间超过osPageCacheBusyTimeOutMills时，参数默认为1000ms，返回流控。
      2. 如果开启transientStorePoolEnable == true，且broker为异步刷盘的主机，且transientStorePool中资源不足，拒绝当前send请求，返回流控。
      3. broker每隔10ms检查send请求队列头部请求的等待时间，如果超过waitTimeMillsInSendQueue，默认200ms，拒绝当前send请求，返回流控。
      4. broker通过拒绝send 请求方式实现流量控制。
   消费者流控，因为消费能力达到瓶颈。消费者流控的结果是降低拉取频率。
      1. 消费者本地缓存消息数超过pullThresholdForQueue时，默认1000。
      2. 消费者本地缓存消息大小超过pullThresholdSizeForQueue时，默认100MB。
      3. 消费者本地缓存消息跨度超过consumeConcurrentlyMaxSpan时，默认2000。
12. 死信队列
    当一条消息初次消费失败，消息队列会自动进行消息重试；达到最大重试次数后，若消费依然失败，消息队列 不会立刻将消息丢弃，而是将其发送到该消费者对应的特殊队列中。
    RocketMQ将这种正常情况下无法被消费的消息称为死信消息（Dead-Letter Message），将存储死信消息的特殊队列称为死信队列（Dead-Letter Queue）。在RocketMQ中，可以通过使用console控制台对死信队列中的消息进行重发来使得消费者实例再次进行消费。

# 2. 架构设计

## 2.1 架构(Architecture)

1. 技术架构，RocketMQ架构上主要分为四部分
   1. Producer：消息发布的角色，支持分布式集群方式部署。Producer通过MQ的负载均衡模块选择相应的Broker集群队列进行消息投递，投递的过程支持快速失败并且低延迟。
   2. Consumer：消息消费的角色，支持分布式集群方式部署。支持以push推，pull拉两种模式对消息进行消费。同时也支持集群方式和广播方式的消费，它提供实时消息订阅机制，可以满足大多数用户的需求。
   3. NameServer：NameServer是一个非常简单的Topic路由注册中心，其角色类似Dubbo中的zookeeper，支持Broker的动态注册与发现。
      主要包括两个功能：Broker管理，路由信息管理
   4. BrokerServer：Broker主要负责消息的存储、投递和查询以及服务高可用保证，为了实现这些功能，Broker包含了以下几个重要子模块。
      1. Remoting Module：整个 Broker 的实体，负责处理来自 clients 端的请求。
      2. Client Manager：负责管理客户端(Producer/Consumer)和维护Consumer的Topic订阅信息
      3. Store Service：提供方便简单的API接口处理消息存储到物理硬盘和查询功能。
      4. HA Service：高可用服务，提供Master Broker 和 Slave Broker之间的数据同步功能。
      5. Index Service：根据特定的Message key对投递到Broker的消息进行索引服务，以提供消息的快速查询。
2. 部署架构，RocketMQ 网络部署特点
   1. NameServer 是一个几乎无状态节点，可集群部署，节点之间无任何信息同步。
   2. Broker 分为 Master 与 Slave，一个Master可以对应多个Slave，但是一个Slave只能对应一个Master。
      Master与Slave 的对应关系通过指定相同的 BrokerName，不同的 BrokerId 来定义，BrokerId 为0表示 Master，非0表示 Slave。
   3. Producer 与 NameServer 集群中的其中一个节点（随机选择）建立长连接，定期从NameServer获取Topic路由信息，并向提供Topic 服务的Master建立长连接，且定时向Master发送心跳。Producer完全无状态，可集群部署。
   4. Consumer 与 NameServer 集群中的其中一个节点（随机选择）建立长连接，定期从NameServer获取Topic路由信息，并向提供Topic服务的Master、Slave建立长连接，且定时向Master、Slave发送心跳。
      Consumer 既可以从 Master订阅消息，也可以从Slave订阅消息，消费者在向Master拉取消息时，Master服务器会根据拉取偏移量与最大偏移量的距离（判断是否读老消息，产生读I/O），以及从服务器是否可读等因素建议下一次是从Master还是Slave拉取。
3. 工作流程
   1. 启动NameServer，NameServer起来后监听端口，等待Broker、Producer、Consumer连上来，相当于一个路由控制中心。
   2. Broker启动，跟所有的NameServer保持长连接，定时发送心跳包。心跳包中包含当前Broker信息(IP+端口等)以及存储所有Topic信息。注册成功后，NameServer集群中就有Topic跟Broker的映射关系。
   3. 收发消息前，先创建Topic，创建Topic时需要指定该Topic要存储在哪些Broker上，也可以在发送消息时自动创建Topic。
   4. Producer发送消息，启动时先跟NameServer集群中的其中一台建立长连接，并从NameServer中获取当前发送的Topic存在哪些Broker上，轮询从队列列表中选择一个队列，然后与队列所在的Broker建立长连接从而向Broker发消息。
   5. Consumer跟Producer类似，跟其中一台NameServer建立长连接，获取当前订阅Topic存在哪些Broker上，然后直接跟Broker建立连接通道，开始消费消息。
   
## 2.2 设计(Design)

1. 消息存储
   1. 消息存储整体架构
      三个消息存储文件
         1. CommitLog：消息主体以及元数据的存储主体，存储Producer端写入的消息主体内容,消息内容不是定长的。
            单个文件大小默认1G ，文件名长度为20位，左边补零，剩余为起始偏移量，比如00000000000000000000代表了第一个文件，起始偏移量为0，文件大小为1G=1073741824；
            消息主要是顺序写入日志文件，当文件满了，写入下一个文件；
            那就是一个 Broker 按顺序写入呗
         2. ConsumeQueue：消息消费队列，引入的目的主要是提高消息消费的性能，由于RocketMQ是基于主题topic的订阅模式，消息消费是针对主题进行的，如果要遍历commitlog文件中根据topic检索消息是非常低效的。
            ConsumeQueue（逻辑消费队列）作为消费消息的索引，保存了指定Topic下的队列消息在CommitLog中的起始物理偏移量offset，消息大小size和消息Tag的HashCode值。
            consumequeue文件夹的组织方式如下：topic/queue/file三层组织结构，文件采取定长设计，每一个条目共20个字节，分别为8字节的commitlog物理偏移量、4字节的消息长度、8字节tag hashcode，单个文件由30W个条目组成，可以像数组一样随机访问每一个条目，每个ConsumeQueue文件大小约5.72M；
         3. IndexFile：IndexFile（索引文件）提供了一种可以通过key或时间区间来查询消息的方法。
            存储为 store\index + 时间戳，每个文件大小约为 400M，底层存储设计为在文件系统中实现HashMap结构，故rocketmq的索引文件其底层实现为hash索引。
   2. 页缓存与内存映射
      页缓存（PageCache)是OS对文件的缓存，用于加速对文件的读写。
      对于数据的写入，OS会先写入至Cache内，随后通过异步的方式由pdflush内核线程将Cache内的数据刷盘至物理磁盘上。
      对于数据的读取，如果一次读取文件时出现未命中PageCache的情况，OS从物理磁盘上访问读取文件的同时，会顺序对其他相邻块的数据文件进行预读取。
      在page cache机制的预读取作用下，Consume Queue文件的读性能几乎接近读内存，即使在有消息堆积情况下也不会影响性能。而对于CommitLog消息存储的日志数据文件来说，读取消息内容时候会产生较多的随机访问读取，严重影响性能。
   3. 消息刷盘
      1. 同步刷盘，只有在消息真正持久化至磁盘后RocketMQ的Broker端才会真正返回给Producer端一个成功的ACK响应。同步刷盘对MQ消息可靠性来说是一种不错的保障，但是性能上会有较大影响，一般适用于金融业务应用该模式较多。
      2. 异步刷盘，能够充分利用OS的PageCache的优势，只要消息写入PageCache即可将成功的ACK返回给Producer端。消息刷盘采用后台异步线程提交的方式进行，降低了读写延迟，提高了MQ的性能和吞吐量。
2. 通信机制
   通讯流程
      1. Broker启动后需要完成一次将自己注册至NameServer的操作；随后每隔30s时间定时向NameServer上报Topic路由信息。
      2. 消息生产者Producer作为客户端发送消息时候，需要根据消息的Topic从本地缓存的TopicPublishInfoTable获取路由信息。
         如果没有则更新路由信息会从NameServer上重新拉取，同时Producer会默认每隔30s向NameServer拉取一次路由信息。
      3. 消息生产者Producer根据 (2) 中获取的路由信息选择一个队列（MessageQueue）进行消息发送；
         Broker作为消息的接收者接收消息并落盘存储。
      4. 消息消费者Consumer根据 (2) 中获取的路由信息，并再完成客户端的负载均衡后，选择其中的某一个或者某几个消息队列来拉取消息并进行消费。
   RocketMQ消息队列自定义了通信协议并在Netty的基础之上扩展了通信模块。
   1. Remoting通信类结构
   2. 协议设计与编解码
   3. 消息的通信方式和流程
   4. Reactor多线程设计
3. 消息过滤
   RocketMQ分布式消息队列的消息过滤方式有别于其它MQ中间件，是在Consumer端订阅消息时再做消息过滤的。
   主要支持如下2种的过滤方式
   1. Tag过滤方式：Consumer端在订阅消息时指定TAG，如果一个消息有多个TAG，可以用||分隔。
      Consumer端会将这个订阅请求构建成一个 SubscriptionData，发送一个Pull消息的请求给Broker端。
      Broker端从 ConsumeQueue读取到一条记录后，会用它记录的消息tag hash值去做过滤
   2. SQL92的过滤方式：这种方式的大致做法和上面的Tag过滤方式一样，只是在Broker端具体过滤过程不太一样
4. 负载均衡
   RocketMQ中的负载均衡都在Client端完成
   1. Producer的负载均衡
      Producer端在发送消息的时候，会先根据Topic找到指定的TopicPublishInfo，在获取了TopicPublishInfo路由信息后，RocketMQ的客户端在默认方式下selectOneMessageQueue()方法会从TopicPublishInfo中的messageQueueList中选择一个队列（MessageQueue）进行发送消息。
   2. Consumer的负载均衡
      在RocketMQ中，Consumer端的两种消费模式（Push/Pull）都是基于拉模式来获取消息的，而在Push模式只是对pull模式的一种封装
      Consumer端负载均衡，即Broker端中多个MessageQueue分配给同一个ConsumerGroup中的哪些Consumer消费。
      1. Consumer端的心跳包发送
         在Consumer启动后，定时向所有Broker实例发送心跳包（其中包含了，消息消费分组名称、订阅关系集合、消息通信模式和客户端id的值等信息）。
         
      2. Consumer端实现负载均衡的核心类—RebalanceImpl
5. 事务消息
   Apache RocketMQ在4.3.0版中已经支持分布式事务消息，这里RocketMQ采用了2PC的思想来实现了提交事务消息，同时增加一个补偿逻辑来处理二阶段超时或者失败的消息，如下图所示。
   1. RocketMQ事务消息流程概要
   2. RocketMQ事务消息设计
6. 消息查询
   RocketMQ支持按照下面两种维度进行消息查询:
   1. 按照MessageId查询消息
      RocketMQ中的MessageId的长度总共有16字节，其中包含了消息存储主机地址（IP地址和端口），消息Commit Log offset。
      按照MessageId查询消息”在RocketMQ中具体做法是：Client端从MessageId中解析出Broker的地址（IP地址和端口）和Commit Log的偏移地址后封装成一个RPC请求后通过Remoting通信层发送
   2. 按照Message Key查询消息
      主要是基于RocketMQ的IndexFile索引文件来实现的。RocketMQ的索引文件逻辑结构，类似JDK中HashMap的实现。

# 3. 样例

## 3.1 样例(Example)

1. 基本样例
   1. 发送消息三种方式：同步消息、异步消息和单向消息。其中前两种消息是可靠的，因为会有发送是否成功的应答。
   2. 接收消息两种方式：pull模式、push模式
2. 顺序消息样例
3. 延时消息样例
4. 批量消息样例
5. 过滤消息样例
6. 消息事务样例
7. Logappender样例
8. OpenMessaging样例

# 4. 最佳实践

## 4.1 最佳实践（Best Practice）

1. 生产者
2. 消费者
3. Broker
4. NameServer
5. 客户端配置
6. 系统配置

## 4.2 消息轨迹指南(Message Trace)
## 4.3 权限管理(Auth Management)
## 4.4 Dledger快速搭建(Quick Start)
## 4.5 集群部署(Cluster Deployment)

# 5. 运维管理

## 5.1 集群部署(Operation)

1. 集群搭建
   1. 单Master模式
      这种方式风险较大，一旦Broker重启或者宕机时，会导致整个服务不可用。不建议线上环境使用,可以用于本地测试。
   2. 多Master模式
      一个集群无Slave，全是Master，例如2个Master或者3个Master，这种模式的优缺点如下：
         1. 优点：配置简单，单个Master宕机或重启维护对应用无影响，在磁盘配置为RAID10时，即使机器宕机不可恢复情况下，由于RAID10磁盘非常可靠，消息也不会丢（异步刷盘丢失少量消息，同步刷盘一条不丢），性能最高；
         2. 缺点：单台机器宕机期间，这台机器上未被消费的消息在机器恢复之前不可订阅，消息实时性会受到影响。
   3. 多Master多Slave模式-异步复制
      每个Master配置一个Slave，有多对Master-Slave，HA采用异步复制方式，主备有短暂消息延迟（毫秒级），这种模式的优缺点如下：
         1. 优点：即使磁盘损坏，消息丢失的非常少，且消息实时性不会受影响，同时Master宕机后，消费者仍然可以从Slave消费，而且此过程对应用透明，不需要人工干预，性能同多Master模式几乎一样；
         2. 缺点：Master宕机，磁盘损坏情况下会丢失少量消息。
   4. 多Master多Slave模式-同步双写
      每个Master配置一个Slave，有多对Master-Slave，HA采用同步双写方式，即只有主备都写成功，才向应用返回成功，这种模式的优缺点如下：
         1. 优点：数据与服务都无单点故障，Master宕机情况下，消息无延迟，服务可用性与数据可用性都非常高；
         2. 缺点：性能比异步复制模式略低（大约低10%左右），发送单个消息的RT会略高，且目前版本在主节点宕机后，备机不能自动切换为主机。
2. mqadmin管理工具
   1. Topic相关
   2. 集群相关
   3. Broker相关
   4. 消息相关
   5. 消费者、消费组相关
   6. 连接相关
   7. NameServer相关
3. 运维常见问题
   1. RocketMQ的mqadmin命令报错问题
   2. RocketMQ生产端和消费端版本不一致导致不能正常消费的问题
   3. 新增一个topic的消费组时，无法消费历史消息的问题
   4. 如何开启从Slave读数据功能
   5. 性能调优问题

# 6. API Reference
