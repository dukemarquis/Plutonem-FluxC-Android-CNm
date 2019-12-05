package com.plutonem.android.fluxc.model.list

import com.plutonem.android.fluxc.model.BuyerModel
import com.plutonem.android.fluxc.model.list.AccountFilter.Everyone
import com.plutonem.android.fluxc.model.list.ListOrder.DESC
import com.plutonem.android.fluxc.model.list.OrderListOrderBy.DATE
import com.plutonem.android.fluxc.model.order.OrderStatus
import com.plutonem.android.fluxc.store.OrderStore.DEFAULT_ORDER_STATUS_LIST

sealed class OrderListDescriptor(
    val buyer: BuyerModel,
    val statusList: List<OrderStatus>,
    val order: ListOrder,
    val orderBy: OrderListOrderBy,
    listConfig: ListConfig
) : ListDescriptor {
    override val config: ListConfig = listConfig

    override val uniqueIdentifier: ListDescriptorUniqueIdentifier by lazy {
        // TODO: need a better hashing algorithm, preferably a perfect hash
        val statusStr = statusList.asSequence().map { it.name }.joinToString(separator = ",")
        when (this) {
            is OrderListDescriptorForRestBuyer -> {
                val accountFilter: String = when (account) {
                    Everyone -> "Everyone"
                }

                ListDescriptorUniqueIdentifier(
                    ("rest-buyer-order-list-${buyer.id}-st$statusStr-a$accountFilter-o${order.value}" +
                            "-ob${orderBy.value}").hashCode()
                )
            }
        }
    }

    override val typeIdentifier: ListDescriptorTypeIdentifier by lazy {
        OrderListDescriptor.calculateTypeIdentifier(buyer.id)
    }

    override fun hashCode(): Int {
        return uniqueIdentifier.value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as OrderListDescriptor
        return uniqueIdentifier == that.uniqueIdentifier
    }

    companion object {
        @JvmStatic
        fun calculateTypeIdentifier(localBuyerId: Int): ListDescriptorTypeIdentifier {
            // TODO: need a better hashing algorithm, preferably a perfect hash
            return ListDescriptorTypeIdentifier("buyer-order-list-$localBuyerId".hashCode())
        }
    }

    class OrderListDescriptorForRestBuyer(
        site: BuyerModel,
        statusList: List<OrderStatus> = DEFAULT_ORDER_STATUS_LIST,
        val account: AccountFilter = AccountFilter.Everyone,
        order: ListOrder = DESC,
        orderBy: OrderListOrderBy = DATE,
        config: ListConfig = ListConfig.default
    ) : OrderListDescriptor(site, statusList, order, orderBy, config)
}

enum class OrderListOrderBy(val value: String) {
    DATE("date"),
    ID("ID");
}