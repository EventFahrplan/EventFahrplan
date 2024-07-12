package nerd.tuxmobil.fahrplan.congress.schedulestatistic

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.database.models.ColumnStatistic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticState.Loading
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticState.Success
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticViewEvent.OnBackClick
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticViewEvent.OnToggleSorting
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExtendWith(MainDispatcherTestExtension::class)
class ScheduleStatisticViewModelTest {

    @Test
    fun `scheduleStatisticState emits Loading initially`() = runTest {
        val repository = createRepository(emptyFlow())
        val viewModel = createViewModel(repository)
        viewModel.scheduleStatisticState.test {
            assertThat(awaitItem()).isEqualTo(Loading)
            expectNoEvents()
        }
    }

    @Test
    fun `scheduleStatisticState emits Success with stats sorted by countNone descending`() =
        runTest {
            val statistic = listOf(
                ColumnStatistic("subtitle", countNone = 0, countPresent = 400),
                ColumnStatistic("title", countNone = 100, countPresent = 300),
            )
            val repository = createRepository(flowOf(statistic))
            val viewModel = createViewModel(repository)
            val expected = Success(
                listOf(
                    ColumnStatistic("title", countNone = 100, countPresent = 300),
                    ColumnStatistic("subtitle", countNone = 0, countPresent = 400),
                )
            )
            viewModel.scheduleStatisticState.test {
                assertThat(awaitItem()).isEqualTo(expected)
                expectNoEvents()
            }
        }

    @Test
    fun `scheduleStatisticState emits Success with stats sorted by name ascending`() = runTest {
        val statistic = listOf(
            ColumnStatistic("title", countNone = 100, countPresent = 300),
            ColumnStatistic("subtitle", countNone = 0, countPresent = 400),
        )
        val repository = createRepository(flowOf(statistic))
        val viewModel = createViewModel(repository)
        viewModel.onViewEvent(OnToggleSorting)
        val expected = Success(
            listOf(
                ColumnStatistic("subtitle", countNone = 0, countPresent = 400),
                ColumnStatistic("title", countNone = 100, countPresent = 300),
            )
        )
        viewModel.scheduleStatisticState.test {
            assertThat(awaitItem()).isEqualTo(expected)
            expectNoEvents()
        }
    }

    @Test
    fun `scheduleStatisticState emits Success with empty stats`() = runTest {
        val statistic = listOf(
            ColumnStatistic("title", countNone = 0, countPresent = 0),
            ColumnStatistic("subtitle", countNone = 0, countPresent = 0),
        )
        val repository = createRepository(flowOf(statistic))
        val viewModel = createViewModel(repository)
        val expected = Success(emptyList())
        viewModel.scheduleStatisticState.test {
            assertThat(awaitItem()).isEqualTo(expected)
            expectNoEvents()
        }
    }

    @Test
    fun `navigateBack emits Unit when OnBackClick`() = runTest {
        val repository = createRepository(emptyFlow())
        val viewModel = createViewModel(repository)
        viewModel.onViewEvent(OnBackClick)
        viewModel.navigateBack.test {
            assertThat(awaitItem()).isEqualTo(Unit)
            expectNoEvents()
        }
    }

    private fun createViewModel(repository: AppRepository) =
        ScheduleStatisticViewModel(repository)

    private fun createRepository(scheduleStatistic: Flow<List<ColumnStatistic>>) =
        mock<AppRepository> {
            on { this.scheduleStatistic } doReturn scheduleStatistic
        }

}
