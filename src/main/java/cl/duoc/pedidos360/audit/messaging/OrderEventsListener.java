package cl.duoc.pedidos360.audit.messaging;

import java.io.IOException;

import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.duoc.pedidos360.audit.config.RabbitMessagingProperties;
import cl.duoc.pedidos360.audit.service.AuditService;

/** Consumidor del dominio auditoría: cola {@code pedidos360.rabbitmq.orders.queue}, alimenta el timeline. */
@Component
public class OrderEventsListener {

    private final EventAcknowledger acknowledger;
    private final AuditService audit;
    private final RabbitMessagingProperties.Subscription subscription;

    public OrderEventsListener(EventAcknowledger acknowledger, AuditService audit,
                               RabbitMessagingProperties properties) {
        this.acknowledger = acknowledger;
        this.audit = audit;
        this.subscription = properties.orders();
    }

    @RabbitListener(id = "audit-order-events", queues = "${pedidos360.rabbitmq.orders.queue}")
    public void onMessage(Message message, Channel channel) throws IOException {
        acknowledger.process(subscription, message, channel, audit::record);
    }
}
