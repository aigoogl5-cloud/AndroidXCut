package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.AndroidxcutTheme
import com.example.viewmodel.EditorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidxcutTheme {
                AndroidxcutApp()
            }
        }
    }
}

@Composable
fun AndroidxcutApp(
    viewModel: EditorViewModel = viewModel()
) {
    val activeProject by viewModel.activeProject.collectAsState()
    val projects by viewModel.allProjects.collectAsState()

    AnimatedContent(
        targetState = activeProject != null,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition",
        modifier = Modifier.fillMaxSize()
    ) { hasActiveProject ->
        if (hasActiveProject && activeProject != null) {
            EditorScreen(
                viewModel = viewModel,
                onBack = { viewModel.closeProject() }
            )
        } else {
            HomeScreen(
                projects = projects,
                onOpenProject = { project -> viewModel.openProject(project) },
                onCreateProject = { title, ratio -> viewModel.createNewProject(title, ratio) },
                onDuplicateProject = { project -> viewModel.duplicateProject(project) },
                onRenameProject = { id, title -> viewModel.renameProject(id, title) },
                onDeleteProject = { id -> viewModel.deleteProject(id) }
            )
        }
    }
}
