package org.neo.gomina.model.component

inline class Knowledge(val knowledge:Int)

interface ComponentKnowledge {

    fun componentKnowledge(componentId: String): List<Pair<String, Knowledge>>
    fun userKnowledge(userId: String): List<Pair<String, Knowledge>>
    fun updateKnowledge(componentId: String, userId: String, knowledge: Knowledge?)

}