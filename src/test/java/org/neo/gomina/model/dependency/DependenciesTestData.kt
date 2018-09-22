package org.neo.gomina.model.dependency


enum class DbMode { WRITE, READ }

val fixin = ProjectDeps(projectId = "fixin",
        used = listOf(
                FunctionUsage("createOrder", "command"),
                FunctionUsage("basketDb", "database", Usage(DbMode.READ))
        )
)
val order = ProjectDeps(projectId = "order",
        exposed = listOf(
                Function("createOrder", "command")
        ),
        used = listOf(
                FunctionUsage("getCustomer", "request"),
                FunctionUsage("createCustomer", "request")
        )
)
val orderExt = ProjectDeps(projectId = "orderExt",
        exposed = listOf(
                Function("createOrder", "command")
        ),
        used = listOf(
                FunctionUsage("getCustomer", "request"),
                FunctionUsage("createCustomer", "request")
        )
)
val basket = ProjectDeps(projectId = "basket",
        exposed = listOf(
                Function("checkBasket", "command")
        ),
        used = listOf(
                FunctionUsage("createOrder", "command"),
                FunctionUsage("checkBasket", "command"),
                FunctionUsage("basketDb", "database", Usage(DbMode.WRITE))
        )
)
val referential = ProjectDeps(projectId = "referential",
        exposed = listOf(
                Function("getCustomer", "request"),
                Function("createCustomer", "request")
        )
)

val projects = listOf(fixin, order, orderExt, basket, referential)
