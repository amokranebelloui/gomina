package org.neo.gomina.model.dependency


enum class DbMode { WRITE, READ }

val fixin = Interactions(serviceId = "fixin",
        used = listOf(
                FunctionUsage("createOrder", "command"),
                FunctionUsage("basketDb", "database", Usage(DbMode.READ))
        )
)
val order = Interactions(serviceId = "order",
        exposed = listOf(
                Function("createOrder", "command"),
                Function("validateOrder", "command")
        ),
        used = listOf(
                FunctionUsage("getCustomer", "request"),
                FunctionUsage("createCustomer", "request"),
                FunctionUsage("cancelOrder", "command")
        )
)
val orderExt = Interactions(serviceId = "orderExt",
        exposed = listOf(
                Function("createOrder", "command"),
                Function("cancelOrder", "command")
        ),
        used = listOf(
                FunctionUsage("getCustomer", "request"),
                FunctionUsage("createCustomer", "request"),
                FunctionUsage("validateOrder", "command")
        )
)
val basket = Interactions(serviceId = "basket",
        exposed = listOf(
                Function("checkBasket", "command")
        ),
        used = listOf(
                FunctionUsage("createOrder", "command"),
                FunctionUsage("checkBasket", "command"),
                FunctionUsage("basketDb", "database", Usage(DbMode.WRITE))
        )
)
val referential = Interactions(serviceId = "referential",
        exposed = listOf(
                Function("getCustomer", "request"),
                Function("createCustomer", "request")
        )
)

val components = listOf(fixin, order, orderExt, basket, referential)
