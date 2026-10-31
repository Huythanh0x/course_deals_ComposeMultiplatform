package com.thanh0x.coursedeals.core.ui.customview

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.thanh0x.coursedeals.core.ui.resources.Res
import com.thanh0x.coursedeals.core.ui.resources.painterResource
import com.thanh0x.coursedeals.core.ui.theme.CourseDealsTheme

class LoadingDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val message = arguments?.getString(ARG_MESSAGE)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CourseDealsTheme {
                    LoadingDialogContent(message = message)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    companion object {
        private const val ARG_MESSAGE = "arg_message"

        fun newInstance(message: String? = null): LoadingDialog {
            val fragment = LoadingDialog()
            val args = Bundle()
            if (message != null) {
                args.putString(ARG_MESSAGE, message)
            }
            fragment.arguments = args
            return fragment
        }
    }
}

@Composable
fun LoadingDialogContent(
    message: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .size(width = 172.dp, height = 172.dp)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Image(
                    painter = painterResource(Res.drawable.ic_logo_mark),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }

            if (!message.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
