package com.minecraft.savagemc.core.api.report;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.route.RouteContext;
import com.minecraft.savagemc.core.api.report.context.ReportContext;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class Report {

    private final int id;
    private final UUID target;

    private final Set<ReportContext> contexts = new HashSet<>();

    private final long createdAt = System.currentTimeMillis(), expiresAt;

    public Report(UUID target, long expiresAt) {
        this.id = Savage.getReportData().list().size() + 1;

        this.target = target;
        this.expiresAt = expiresAt;

        Savage.getReportData().save(this);
    }

    protected void update() {
        Savage.getReportData().update(this);
    }

    public void delete() {
        Savage.getReportData().delete(this);
    }

    public String getTargetName() {
        Account account = Savage.getAccountController().of(target);

        return account != null ? account.getNickname() : "...";
    }

    /* Route */
    public RouteContext getRoute() {
        Account account = Savage.getAccountData().of(target);

        return account != null ? account.getRoute() : RouteContext.builder().build();
    }

    /* Context System */
    public ReportContext getContext(UUID sender) {
        return contexts.stream()
                .filter(context -> context.isValid() && context.getSender().equals(sender))
                .findFirst()
                .orElse(null);
    }

    public boolean hasContext(UUID sender) {
        return getContext(sender) != null;
    }

    public void addContext(ReportContext context) {
        contexts.add(context);
        update();
    }

    public void removeContext(ReportContext context) {
        contexts.remove(context);
        update();
    }

    /* Methods to Check */
    public boolean hasExpired() {
        return expiresAt < System.currentTimeMillis();
    }
}
