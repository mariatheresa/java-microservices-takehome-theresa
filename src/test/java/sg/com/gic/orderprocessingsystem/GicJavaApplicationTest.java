package sg.com.gic.orderprocessingsystem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import sg.com.gic.orderprocessingsystem.eventbus.EventPublisher;
import sg.com.gic.orderprocessingsystem.eventbus.EventSubscriber;
import sg.com.gic.orderprocessingsystem.eventbus.JpaEventPublisher;
import sg.com.gic.orderprocessingsystem.notification.controller.NotificationController;
import sg.com.gic.orderprocessingsystem.notification.listener.PaymentSucceededEventListener;
import sg.com.gic.orderprocessingsystem.notification.service.NotificationService;
import sg.com.gic.orderprocessingsystem.order.controller.OrderController;
import sg.com.gic.orderprocessingsystem.order.service.OrderService;
import sg.com.gic.orderprocessingsystem.payment.controller.PaymentController;
import sg.com.gic.orderprocessingsystem.payment.listener.OrderCreatedEventListener;
import sg.com.gic.orderprocessingsystem.payment.service.PaymentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DisplayName("Application Context Tests")
class GicJavaApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should load application context successfully")
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    @DisplayName("Should have OrderController bean")
    void shouldHaveOrderControllerBean() {
        OrderController controller = applicationContext.getBean(OrderController.class);
        assertNotNull(controller);
    }

    @Test
    @DisplayName("Should have OrderService bean")
    void shouldHaveOrderServiceBean() {
        OrderService service = applicationContext.getBean(OrderService.class);
        assertNotNull(service);
    }

    @Test
    @DisplayName("Should have PaymentController bean")
    void shouldHavePaymentControllerBean() {
        PaymentController controller = applicationContext.getBean(PaymentController.class);
        assertNotNull(controller);
    }

    @Test
    @DisplayName("Should have PaymentService bean")
    void shouldHavePaymentServiceBean() {
        PaymentService service = applicationContext.getBean(PaymentService.class);
        assertNotNull(service);
    }

    @Test
    @DisplayName("Should have NotificationController bean")
    void shouldHaveNotificationControllerBean() {
        NotificationController controller = applicationContext.getBean(NotificationController.class);
        assertNotNull(controller);
    }

    @Test
    @DisplayName("Should have NotificationService bean")
    void shouldHaveNotificationServiceBean() {
        NotificationService service = applicationContext.getBean(NotificationService.class);
        assertNotNull(service);
    }

    @Test
    @DisplayName("Should have JpaEventPublisher bean")
    void shouldHaveJpaEventPublisherBean() {
        JpaEventPublisher eventBus = applicationContext.getBean(JpaEventPublisher.class);
        assertNotNull(eventBus);
    }

    @Test
    @DisplayName("Should have EventPublisher bean")
    void shouldHaveEventPublisherBean() {
        EventPublisher publisher = applicationContext.getBean(EventPublisher.class);
        assertNotNull(publisher);
    }

    @Test
    @DisplayName("Should have EventSubscriber bean")
    @Disabled("EventSubscriber removed - using Spring @EventListener now")
    void shouldHaveEventSubscriberBean() {
        EventSubscriber subscriber = applicationContext.getBean(EventSubscriber.class);
        assertNotNull(subscriber);
    }

    @Test
    @DisplayName("Should have OrderCreatedEventListener bean")
    void shouldHaveOrderCreatedEventListenerBean() {
        OrderCreatedEventListener listener = applicationContext.getBean(OrderCreatedEventListener.class);
        assertNotNull(listener);
    }

    @Test
    @DisplayName("Should have PaymentSucceededEventListener bean")
    void shouldHavePaymentSucceededEventListenerBean() {
        PaymentSucceededEventListener listener = applicationContext.getBean(PaymentSucceededEventListener.class);
        assertNotNull(listener);
    }

    @Test
    @DisplayName("Should have all required beans")
    void shouldHaveAllRequiredBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        assertThat(beanNames).isNotEmpty();
        assertThat(beanNames.length).isGreaterThan(10);
    }

    @Test
    @DisplayName("EventBus should implement both EventPublisher")
    void eventBusShouldImplementBothInterfaces() {
        JpaEventPublisher eventBus = applicationContext.getBean(JpaEventPublisher.class);
        assertThat(eventBus).isInstanceOf(EventPublisher.class);
    }

    @Test
    @DisplayName("Should have correct bean count for controllers")
    void shouldHaveCorrectBeanCountForControllers() {
        String[] controllerBeans = applicationContext.getBeanNamesForAnnotation(
                org.springframework.web.bind.annotation.RestController.class
        );
        assertThat(controllerBeans).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should have correct bean count for services")
    void shouldHaveCorrectBeanCountForServices() {
        String[] serviceBeans = applicationContext.getBeanNamesForAnnotation(
                org.springframework.stereotype.Service.class
        );
        assertThat(serviceBeans).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should have correct bean count for components")
    void shouldHaveCorrectBeanCountForComponents() {
        String[] componentBeans = applicationContext.getBeanNamesForAnnotation(
                org.springframework.stereotype.Component.class
        );
        assertThat(componentBeans).hasSizeGreaterThanOrEqualTo(3);
    }
}

