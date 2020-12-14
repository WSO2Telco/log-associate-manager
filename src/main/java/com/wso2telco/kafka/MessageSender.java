package com.wso2telco.kafka;

import com.wso2telco.util.CommonConstant;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.wso2telco.util.CommonConstant.AXP_ANALYTICS_LOGGER;
import static com.wso2telco.util.CommonConstant.kafkaEnabled;

public class MessageSender {

    public void sendMessage(String transactionLog) {
        ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(CommonConstant.MAX_THREAD_COUNT));
        Runnable worker = new KafkaThreadCreator(transactionLog);
        executor.execute(worker);
    }

    public static class KafkaThreadCreator implements Runnable {
        private final String transactionLog;

        KafkaThreadCreator(String transactionLog) {
            this.transactionLog = transactionLog;
        }

        @Override
        public void run() {
            if (kafkaEnabled) {
                com.wso2telco.kafka.KafkaProducer kafkaProducer = new com.wso2telco.kafka.KafkaProducer();
                Producer<String, String> producer = kafkaProducer.createKafkaProducer();
                int sendMessageCount = 1;

                long time = System.currentTimeMillis();

                try {
                    for (long index = time; index < time + sendMessageCount; index++) {
                        final ProducerRecord<String, String> record =
                                new ProducerRecord<String, String>(CommonConstant.KAFKA_TOPIC, String.valueOf(index),
                                        transactionLog);
                        producer.send(record, new Callback() {
                            public void onCompletion(RecordMetadata metadata, Exception e) {
                                if (e != null) {
                                    AXP_ANALYTICS_LOGGER.info(transactionLog);
                                }
                            }
                        });
                    }

                } finally {
                    producer.flush();
                    producer.close();
                }
            } else {
                AXP_ANALYTICS_LOGGER.info(transactionLog);
            }
        }
    }
}