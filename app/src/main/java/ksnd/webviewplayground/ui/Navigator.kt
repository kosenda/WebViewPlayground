package ksnd.webviewplayground.ui

import androidx.navigation3.runtime.NavKey

/**
 * ナビゲーション状態を更新してナビゲーションイベント（進むと戻る）を処理する
 * ref: https://developer.android.com/guide/navigation/navigation-3/recipes/multiple-backstacks?hl=ja
 */
class Navigator(val state: NavigationState) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys){
            // トップレベルルートの場合は、単にそれに切り替える
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute] ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        // 現在のルートのベースにいる場合は、スタートルートスタックに戻る
        if (currentRoute == state.topLevelRoute){
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}