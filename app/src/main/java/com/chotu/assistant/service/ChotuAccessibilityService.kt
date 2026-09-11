package com.chotu.assistant.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ChotuAccessibilityService : AccessibilityService() {

    companion object {
        var instance: ChotuAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Continuous Real-Time Screen Monitoring
    }

    override fun onInterrupt() {
        instance = null
    }

    fun dumpScreenTextTree(): String {
        val rootNode = rootInActiveWindow ?: return "Empty Screen"
        val sb = StringBuilder()
        traverseNodeTree(rootNode, sb)
        return sb.toString()
    }

    private fun traverseNodeTree(node: AccessibilityNodeInfo?, sb: StringBuilder) {
        if (node == null) return
        
        val text = node.text
        val viewId = node.viewIdResourceName
        
        if (!text.isNull_orEmpty()) {
            sb.append("[Node: $text | ID: ${viewId ?: "none"}]\n")
        }

        for (i in 0 until node.childCount) {
            traverseNodeTree(node.getChild(i), sb)
        }
    }

    fun performClickByText(targetText: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByText(targetText)
        if (!nodes.isNullOrEmpty()) {
            for (node in nodes) {
                if (node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
            }
        }
        return false
    }

    private fun CharSequence?.isNull_orEmpty(): Boolean = this == null || this.isEmpty()
}