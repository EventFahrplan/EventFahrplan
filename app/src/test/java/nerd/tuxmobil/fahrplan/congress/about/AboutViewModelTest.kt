package nerd.tuxmobil.fahrplan.congress.about

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.about.AboutViewEvent.OnPostalAddressClick
import nerd.tuxmobil.fahrplan.congress.commons.ExternalNavigation
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExtendWith(MainDispatcherTestExtension::class)
class AboutViewModelTest {

    @Test
    fun `aboutParameter property emits AboutParameter at init`() = runTest {
        val aboutParameterFactory = mock<AboutParameterFactory> {
            on { createAboutParameter(any()) } doReturn AboutParameter()
        }
        val viewModel = createViewModel(aboutParameterFactory = aboutParameterFactory)
        viewModel.aboutParameter.test {
            assertThat(awaitItem()).isEqualTo(AboutParameter())
        }
    }

    @Test
    fun `onViewEvent(OnPostalAddressClick) invokes openMap`() = runTest {
        val externalNavigation = mock<ExternalNavigation>()
        val viewModel = createViewModel(externalNavigation = externalNavigation)
        viewModel.onViewEvent(OnPostalAddressClick("Street 1, City"))
        verifyInvokedOnce(externalNavigation).openMap("Street 1, City")
    }

    private fun createViewModel(
        repository: AppRepository = createRepository(),
        externalNavigation: ExternalNavigation = mock(),
        aboutParameterFactory: AboutParameterFactory = mock(),
    ) = AboutViewModel(
        repository = repository,
        executionContext = TestExecutionContext,
        externalNavigation = externalNavigation,
        aboutParameterFactory = aboutParameterFactory,
    )

    private fun createRepository() = mock<AppRepository> {
        on { readMeta() } doReturn Meta()
    }

}
