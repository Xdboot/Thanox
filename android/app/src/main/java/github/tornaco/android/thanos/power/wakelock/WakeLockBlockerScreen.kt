/*
 * (C) Copyright 2022 Thanox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)

package github.tornaco.android.thanos.power.wakelock

import androidx.compose.animation.*
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import github.tornaco.android.thanos.R
import github.tornaco.android.thanos.module.compose.common.loader.AppSetFilterItem
import github.tornaco.android.thanos.module.compose.common.requireActivity
import github.tornaco.android.thanos.module.compose.common.theme.TypographyDefaults
import github.tornaco.android.thanos.module.compose.common.widget.*

@Composable
fun WakeLockBlockerScreen(onBackPressed: () -> Unit) {
    val viewModel =
        hiltViewModel<WakeLockBlockerViewModel>(LocalContext.current.requireActivity())
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    viewModel.bindLifecycle(lifecycle)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.init()
    }

    ThanoxSmallAppBarScaffold(
        title = {
            Text(
                stringResource(id = R.string.feature_title_wakelock_blocker),
                style = TypographyDefaults.appBarTitleTextStyle()
            )
        },
        actions = {
            Row(
                modifier = Modifier,
                verticalAlignment = CenterVertically
            ) {

            }
        },
        onBackPressed = onBackPressed
    ) { contentPadding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(state.isLoading),
            onRefresh = { viewModel.refresh() },
            indicatorPadding = contentPadding,
            clipIndicatorToPadding = false,
            indicator = { state, refreshTriggerDistance ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = refreshTriggerDistance,
                    scale = true,
                    arrowEnabled = false,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                SwitchBar(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    isChecked = state.isWakeLockBlockerEnabled
                ) {
                    viewModel.setWakeLockBlockerEnabled(it)
                }

                WakeLockList(
                    contentPadding = PaddingValues(top = 16.dp),
                    state = state,
                    onFilterItemSelected = { viewModel.onFilterItemSelected(it) },
                    blockWakeLock = { wakelock, block ->
                        viewModel.setBlockWakeLock(wakelock, block)
                    },
                    batchSelect = {
                        viewModel.batchSelect(it)
                    })
            }

        }
    }
}

@Composable
private fun WakeLockList(
    contentPadding: PaddingValues,
    state: BlockerState,
    onFilterItemSelected: (AppSetFilterItem) -> Unit,
    blockWakeLock: (WakeLockUiModel, Boolean) -> Unit,
    batchSelect: (PackageState) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                AppFilterDropDown(
                    state,
                    onFilterItemSelected
                )
                Spacer(modifier = Modifier.size(16.dp))
            }
        }

        items(state.packageStates) { item ->
            val expandState = remember { mutableStateOf(ExpandableState.Collapsed) }
            ExpandableContainer(
                expandState = expandState,
                mainContent = {
                    AppInfoItem(item, expandState.value == ExpandableState.Expand, batchSelect)
                },
                expandContent = {
                    WakeLockListItem(item, blockWakeLock)
                })
        }
    }
}


@Composable
private fun AppInfoItem(
    packageState: PackageState,
    isExpand: Boolean,
    batchSelect: (PackageState) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .heightIn(min = 72.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(end = 12.dp),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            AppIcon(modifier = Modifier.size(38.dp), packageState.appInfo)
            Spacer(modifier = Modifier.size(12.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Row {
                    Text(
                        modifier = Modifier.alignByBaseline(),
                        text = packageState.appInfo.appLabel,
                        fontSize = 16.sp
                    )
                    if (!packageState.appInfo.isCurrentUser) {
                        SmallSpacer()
                        Text(
                            modifier = Modifier.alignByBaseline(),
                            text = stringResource(
                                id = R.string.module_common_user,
                                packageState.appInfo.userId
                            ),
                            fontSize = 10.sp
                        )
                    }
                }

                SmallSpacer()
                Text(
                    text = stringResource(
                        id = R.string.wakelock_count,
                        packageState.wakeLocks.size
                    ),
                    fontSize = 14.sp
                )
            }
        }

        val appInfoItemAction =
            when {
                isExpand -> AppInfoItemAction.BatchSelectButton
                !isExpand && packageState.hasBlock -> AppInfoItemAction.BlockIcon(isAllBlocked = packageState.isAllBlock)
                else -> AppInfoItemAction.Noop
            }
        AnimatedContent(targetState = appInfoItemAction,
            transitionSpec = {
                fadeIn(animationSpec = tween(150, 150)) with
                        fadeOut(animationSpec = tween(150)) using
                        SizeTransform { initialSize, targetSize ->
                            if (targetState != AppInfoItemAction.Noop) {
                                keyframes {
                                    // Expand horizontally first.
                                    IntSize(targetSize.width, initialSize.height) at 150
                                    durationMillis = 300
                                }
                            } else {
                                keyframes {
                                    // Shrink vertically first.
                                    IntSize(initialSize.width, targetSize.height) at 150
                                    durationMillis = 300
                                }
                            }
                        }
            }) { action ->
            when (action) {
                AppInfoItemAction.BatchSelectButton -> {
                    FilledTonalButton(onClick = {
                        batchSelect(packageState)
                    }) {
                        Text(text = stringResource(id = R.string.common_menu_title_batch_select))
                    }
                }
                is AppInfoItemAction.BlockIcon -> {
                    if (action.isAllBlocked) {
                        Icon(
                            imageVector = Icons.Outlined.Block,
                            tint = Color.Red,
                            contentDescription = "Blocked"
                        )
                    } else {
                        Icon(imageVector = Icons.Outlined.Block, contentDescription = "Blocked")
                    }
                }
                AppInfoItemAction.Noop -> {}
            }
        }
    }
}

sealed interface AppInfoItemAction {
    object BatchSelectButton : AppInfoItemAction
    data class BlockIcon(val isAllBlocked: Boolean) : AppInfoItemAction
    object Noop : AppInfoItemAction
}

@Composable
fun WakeLockListItem(
    packageState: PackageState,
    blockWakeLock: (WakeLockUiModel, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        packageState.wakeLocks.forEach { wakeLock ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableWithRipple {

                    }
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .heightIn(min = 64.dp),
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = CenterVertically
                ) {
                    Icon(
                        modifier = Modifier,
                        imageVector = Icons.Outlined.FileCopy, contentDescription = null
                    )
                    Spacer(modifier = Modifier.size(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = wakeLock.tag,
                            fontSize = 14.sp
                        )
                    }

                    if (wakeLock.isHeld) {
                        MD3Badge(
                            text = stringResource(id = R.string.wakelock_state_holding)
                        )
                    }
                }

                WakeLockCheckBox(wakeLock, blockWakeLock)
            }
        }
    }
}

@Composable
private fun WakeLockCheckBox(
    wakeLock: WakeLockUiModel,
    blockWakeLock: (WakeLockUiModel, Boolean) -> Unit
) {
    var isChecked by remember(wakeLock) {
        mutableStateOf(wakeLock.isBlock)
    }
    Checkbox(checked = isChecked, onCheckedChange = {
        blockWakeLock(wakeLock, it)
        isChecked = it
    })
}

@Composable
private fun AppFilterDropDown(
    state: BlockerState,
    onFilterItemSelected: (AppSetFilterItem) -> Unit
) {
    FilterDropDown(
        icon = Icons.Filled.FilterAlt,
        selectedItem = state.selectedAppSetFilterItem,
        allItems = state.appFilterItems,
        onItemSelected = onFilterItemSelected
    )
}