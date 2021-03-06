package di.container.oksianiuk.anton.model.impl;

import di.container.oksianiuk.anton.model.Distributor;
import di.container.oksianiuk.anton.model.Gift;
import di.container.oksianiuk.anton.model.PaymentSystem;
import di.container.oksianiuk.anton.model.User;

public class GiftDistributorWithoutInjectAnnotation implements Distributor {
    private PaymentSystem paymentSystem;

    private Gift gift;

    private User user;


    public GiftDistributorWithoutInjectAnnotation(PaymentSystem paymentSystem, Gift gift, User user) {
        this.paymentSystem = paymentSystem;
        this.gift = gift;
        this.user = user;
    }

    public GiftDistributorWithoutInjectAnnotation() {
    }

    @Override
    public void distributeGift() {
        paymentSystem.pay();
        System.out.println(String.format("Person %s got his gift %s", user.getUsername(), gift.getName()));
    }
}