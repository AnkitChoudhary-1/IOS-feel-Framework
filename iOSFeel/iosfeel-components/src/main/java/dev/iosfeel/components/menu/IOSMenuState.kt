package dev.iosfeel.components.menu

import androidx.compose.runtime.Composable
import dev.iosfeel.core.tokens.IOSActionRole

interface IOSMenuScope {
    fun item(
        label: String,
        icon: String? = null,
        role: IOSActionRole = IOSActionRole.Normal,
        onClick: () -> Unit
    )
    fun separator()
}

class IOSMenuScopeImpl : IOSMenuScope {
    internal val items = mutableListOf<MenuElement>()

    override fun item(label: String, icon: String?, role: IOSActionRole, onClick: () -> Unit) {
        items.add(MenuElement.Action(IOSMenuItem(label, icon, role, onClick)))
    }

    override fun separator() {
        items.add(MenuElement.Separator)
    }
}

sealed interface MenuElement {
    data class Action(val item: IOSMenuItem) : MenuElement
    data object Separator : MenuElement
}
