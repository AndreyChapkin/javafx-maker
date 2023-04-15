package org.my.util

import javafx.scene.image.Image

object Assets {

    val TO_DO_ITEM_NEED_ATTENTION_ICON: Image by lazy {
        Image("/org/my/component/assets/flag-crimson.png")
    }
    val TO_DO_ITEM_IN_PROGRESS_ICON: Image by lazy {
        Image("/org/my/component/assets/flag-cyan.png")
    }
    val TO_DO_ITEM_ON_HOLD_ICON: Image by lazy {
        Image("/org/my/component/assets/flag-violet.png")
    }
}