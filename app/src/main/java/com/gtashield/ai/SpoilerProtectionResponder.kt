
package com.gtashield.ai

import android.content.Context

class SpoilerProtectionResponder(context: Context) {

    private val overlayManager =
        SpoilerOverlayManager(context)

    fun handle(
        report: SpoilerAnalysisReport,
        onAction: (SpoilerAction, String) -> Unit = {}
    ) {

        when (report.decision.action) {

            SpoilerAction.BLOCK -> {

                val shown = overlayManager.show(
                    "GTA 6 CONTENT DETECTED"
                )

                if (shown) {
                    onAction(
                        SpoilerAction.BLOCK,
                        "GTA 6 content detected. Screen covered."
                    )
                } else {
                    onAction(
                        SpoilerAction.BLOCK,
                        "GTA 6 content detected, but overlay permission is unavailable."
                    )
                }
            }

            SpoilerAction.REVIEW -> {

                onAction(
                    SpoilerAction.REVIEW,
                    "Possible gaming content detected. GTA 6 is not confirmed."
                )
            }

            SpoilerAction.ALLOW -> {

                overlayManager.hide()

                onAction(
                    SpoilerAction.ALLOW,
                    "No sufficient GTA 6 evidence detected."
                )
            }
        }
    }

    fun hideProtection() {
        overlayManager.hide()
    }

    fun isOverlayVisible(): Boolean {
        return overlayManager.isVisible()
    }
}
