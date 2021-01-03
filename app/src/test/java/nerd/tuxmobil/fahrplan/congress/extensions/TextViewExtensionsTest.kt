package nerd.tuxmobil.fahrplan.congress.extensions

import android.text.Spannable
import android.text.method.MovementMethod
import android.text.style.URLSpan
import android.widget.TextView
import androidx.core.text.set
import androidx.core.text.toSpannable
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.mockito.ArgumentCaptor

class TextViewExtensionsTest {

    @Test
    fun `setLinkText applies formatted link text with title and MovementMethod and LinkTextColor`() {
        val textView = mock<TextView>()
        val movementMethod = mock<MovementMethod>()
        val plainLinkUrl = "https://example.com"
        val urlTitle = "Example website"
        textView.setLinkText(
                plainLinkUrl = plainLinkUrl,
                urlTitle = urlTitle,
                movementMethod = movementMethod,
                linkTextColor = 23
        )
        val linkTextSpannableCaptor = ArgumentCaptor.forClass(Spannable::class.java)
        verify(textView).setText(linkTextSpannableCaptor.capture(), any())
        verify(textView).movementMethod = movementMethod
        verify(textView).setLinkTextColor(23)
        val linkText = urlTitle.toSpannable().apply { set(0, urlTitle.length, URLSpan(plainLinkUrl)) }
        assertThat(linkTextSpannableCaptor.value.toString()).isEqualTo(linkText.toString())
    }

    @Test
    fun `setLinkText applies formatted link text without title and MovementMethod and LinkTextColor`() {
        val textView = mock<TextView>()
        val movementMethod = mock<MovementMethod>()
        val plainLinkUrl = "https://example.com"
        textView.setLinkText(
                plainLinkUrl = plainLinkUrl,
                movementMethod = movementMethod,
                linkTextColor = 23
        )
        val linkTextSpannableCaptor = ArgumentCaptor.forClass(Spannable::class.java)
        verify(textView).setText(linkTextSpannableCaptor.capture(), any())
        verify(textView).movementMethod = movementMethod
        verify(textView).setLinkTextColor(23)
        val linkText = plainLinkUrl.toSpannable().apply { set(0, plainLinkUrl.length, URLSpan(plainLinkUrl)) }
        assertThat(linkTextSpannableCaptor.value.toString()).isEqualTo(linkText.toString())
    }

}
