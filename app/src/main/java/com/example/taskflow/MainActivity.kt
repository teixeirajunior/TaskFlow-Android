package com.example.taskflow

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

// ======================================================
// MODELO DE TAREFA
// ======================================================

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val category: String,
    val completed: Boolean = false
)

// ======================================================
// ACTIVITY PRINCIPAL
// ======================================================

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TaskFlowApp()
        }
    }
}

// ======================================================
// CORES
// ======================================================

private val BluePrimary = Color(0xFF2563EB)
private val BlueDark = Color(0xFF1E3A8A)
private val BackgroundColor = Color(0xFFF1F5F9)
private val CardColor = Color.White
private val TextDark = Color(0xFF1E293B)
private val TextGray = Color(0xFF64748B)
private val GreenColor = Color(0xFF16A34A)
private val RedColor = Color(0xFFDC2626)

// ======================================================
// APLICATIVO PRINCIPAL
// ======================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFlowApp() {

    val context = androidx.compose.ui.platform.LocalContext.current

    // Lista de tarefas carregadas do armazenamento local
    val tasks = remember {
        mutableStateListOf<Task>().apply {
            addAll(loadTasks(context))
        }
    }

    // Controle do formulário
    var showDialog by remember {
        mutableStateOf(false)
    }

    var taskBeingEdited by remember {
        mutableStateOf<Task?>(null)
    }

    // Filtro da lista
    var selectedFilter by remember {
        mutableStateOf("Todas")
    }

    // IDs das tarefas selecionadas
    val selectedTaskIds = remember {
        mutableStateListOf<String>()
    }

    // Contadores
    val totalTasks = tasks.size

    val completedTasks = tasks.count {
        it.completed
    }

    val pendingTasks = tasks.count {
        !it.completed
    }

    // Filtro de tarefas
    val filteredTasks = when (selectedFilter) {

        "Pendentes" -> tasks.filter {
            !it.completed
        }

        "Concluídas" -> tasks.filter {
            it.completed
        }

        else -> tasks.toList()
    }

    // ==================================================
    // CONCLUIR TAREFAS SELECIONADAS
    // ==================================================

    fun completeSelectedTasks() {

        val selectedIds = selectedTaskIds.toSet()

        for (i in tasks.indices) {

            if (tasks[i].id in selectedIds) {

                tasks[i] = tasks[i].copy(
                    completed = true
                )
            }
        }

        saveTasks(context, tasks)

        selectedTaskIds.clear()
    }

    // ==================================================
    // TEMA
    // ==================================================

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = BluePrimary,
            secondary = GreenColor,
            background = BackgroundColor
        )
    ) {

        Scaffold(
            containerColor = BackgroundColor,

            floatingActionButton = {

                FloatingActionButton(
                    onClick = {
                        taskBeingEdited = null
                        showDialog = true
                    },
                    containerColor = BluePrimary,
                    contentColor = Color.White
                ) {

                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }

        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
            ) {

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // CABEÇALHO
                Text(
                    text = "TASKFLOW",
                    color = BluePrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Olá! 👋",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Text(
                    text = "Organize suas tarefas e seu dia.",
                    color = TextGray,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // ==================================================
                // RESUMO
                // ==================================================

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BlueDark
                    )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {

                        Text(
                            text = "Resumo das tarefas",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            SummaryItem(
                                label = "Total",
                                value = totalTasks.toString()
                            )

                            SummaryItem(
                                label = "Pendentes",
                                value = pendingTasks.toString()
                            )

                            SummaryItem(
                                label = "Concluídas",
                                value = completedTasks.toString()
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        val progress = if (totalTasks > 0) {
                            completedTasks.toFloat() / totalTasks.toFloat()
                        } else {
                            0f
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Color(0xFF86EFAC),
                            trackColor = Color(0xFF475569)
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "${(progress * 100).toInt()}% concluído",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                // ==================================================
                // TÍTULO + BOTÃO CONCLUIR SELECIONADAS
                // ==================================================

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Minhas tarefas",
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    if (selectedTaskIds.isNotEmpty()) {

                        Button(
                            onClick = {
                                completeSelectedTasks()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenColor
                            )
                        ) {

                            Text(
                                text = "Concluir (${selectedTaskIds.size})"
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                // ==================================================
                // FILTROS
                // ==================================================

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    FilterButton(
                        text = "Todas",
                        selected = selectedFilter == "Todas",
                        onClick = {
                            selectedFilter = "Todas"
                        }
                    )

                    FilterButton(
                        text = "Pendentes",
                        selected = selectedFilter == "Pendentes",
                        onClick = {
                            selectedFilter = "Pendentes"
                        }
                    )

                    FilterButton(
                        text = "Concluídas",
                        selected = selectedFilter == "Concluídas",
                        onClick = {
                            selectedFilter = "Concluídas"
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                // ==================================================
                // LISTA
                // ==================================================

                if (filteredTasks.isEmpty()) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "📋",
                                style = MaterialTheme.typography.displayMedium
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = "Nenhuma tarefa encontrada",
                                color = TextGray
                            )

                            Text(
                                text = "Clique no botão + para adicionar",
                                color = TextGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                } else {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),

                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(
                            items = filteredTasks,
                            key = { it.id }
                        ) { task ->

                            TaskCard(
                                task = task,

                                // SELEÇÃO
                                selected = task.id in selectedTaskIds,

                                onSelect = {

                                    if (task.id in selectedTaskIds) {

                                        selectedTaskIds.remove(task.id)

                                    } else {

                                        selectedTaskIds.add(task.id)
                                    }
                                },

                                // CONCLUSÃO INDIVIDUAL
                                onToggle = {

                                    val index = tasks.indexOfFirst {
                                        it.id == task.id
                                    }

                                    if (index != -1) {

                                        tasks[index] = tasks[index].copy(
                                            completed = !tasks[index].completed
                                        )

                                        saveTasks(context, tasks)

                                        selectedTaskIds.remove(task.id)
                                    }
                                },

                                // EDITAR
                                onEdit = {

                                    taskBeingEdited = task
                                    showDialog = true
                                },

                                // EXCLUIR
                                onDelete = {

                                    tasks.removeAll {
                                        it.id == task.id
                                    }

                                    selectedTaskIds.remove(task.id)

                                    saveTasks(context, tasks)
                                }
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }
        }

        // ==================================================
        // FORMULÁRIO
        // ==================================================

        if (showDialog) {

            TaskDialog(
                task = taskBeingEdited,

                onDismiss = {

                    showDialog = false
                    taskBeingEdited = null
                },

                onSave = { newTask ->

                    if (taskBeingEdited == null) {

                        tasks.add(newTask)

                    } else {

                        val index = tasks.indexOfFirst {
                            it.id == taskBeingEdited!!.id
                        }

                        if (index != -1) {
                            tasks[index] = newTask
                        }
                    }

                    saveTasks(context, tasks)

                    showDialog = false
                    taskBeingEdited = null
                }
            )
        }
    }
}

// ======================================================
// ITEM DO RESUMO
// ======================================================

@Composable
fun SummaryItem(
    label: String,
    value: String
) {

    Column {

        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            color = Color(0xFFDBEAFE),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// ======================================================
// FILTRO
// ======================================================

@Composable
fun FilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(text)
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BluePrimary,
            selectedLabelColor = Color.White
        )
    )
}

// ======================================================
// CARTÃO DA TAREFA
// ======================================================

@Composable
fun TaskCard(
    task: Task,
    selected: Boolean,
    onSelect: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),

        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                Color(0xFFEFF6FF)
            } else {
                CardColor
            }
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            // SELEÇÃO PARA AÇÃO EM LOTE
            Checkbox(
                checked = selected,
                onCheckedChange = {
                    onSelect()
                }
            )

            // CONCLUSÃO INDIVIDUAL
            Checkbox(
                checked = task.completed,
                onCheckedChange = {
                    onToggle()
                },

                colors = CheckboxDefaults.colors(
                    checkedColor = GreenColor
                )
            )

            Spacer(
                modifier = Modifier.width(6.dp)
            )

            // INFORMAÇÕES
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,

                    textDecoration = if (task.completed) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )

                if (task.description.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = task.description,
                        color = TextGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "📅 ${task.date}  •  ⏰ ${task.time}",
                    color = TextGray,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "📂 ${task.category}",
                    color = BluePrimary,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // BOTÕES
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TextButton(
                    onClick = onEdit
                ) {
                    Text("Editar")
                }

                TextButton(
                    onClick = onDelete
                ) {
                    Text(
                        text = "Excluir",
                        color = RedColor
                    )
                }
            }
        }
    }
}

// ======================================================
// FORMULÁRIO
// ======================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {

    var title by remember(task) {
        mutableStateOf(task?.title ?: "")
    }

    var description by remember(task) {
        mutableStateOf(task?.description ?: "")
    }

    var date by remember(task) {
        mutableStateOf(task?.date ?: "")
    }

    var time by remember(task) {
        mutableStateOf(task?.time ?: "")
    }

    var category by remember(task) {
        mutableStateOf(task?.category ?: "Pessoal")
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    val categories = listOf(
        "Pessoal",
        "Estudos",
        "Trabalho",
        "Saúde",
        "Financeiro",
        "Outros"
    )

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = if (task == null) {
                    "Nova tarefa"
                } else {
                    "Editar tarefa"
                }
            )
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    label = {
                        Text("Nome da tarefa")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = {
                        Text("Descrição")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                    },
                    label = {
                        Text("Data (ex.: 20/11/2026)")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = {
                        time = it
                    },
                    label = {
                        Text("Horário (ex.: 14:30)")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // CATEGORIA
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    }
                ) {

                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Categoria")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {

                        categories.forEach { item ->

                            DropdownMenuItem(
                                text = {
                                    Text(item)
                                },

                                onClick = {

                                    category = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    if (title.isNotBlank()) {

                        val newTask = Task(
                            id = task?.id ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            description = description.trim(),
                            date = date.trim(),
                            time = time.trim(),
                            category = category,
                            completed = task?.completed ?: false
                        )

                        onSave(newTask)
                    }
                }
            ) {

                Text("Salvar")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("Cancelar")
            }
        }
    )
}

// ======================================================
// SALVAR TAREFAS
// ======================================================

fun saveTasks(
    context: Context,
    tasks: List<Task>
) {

    val jsonArray = JSONArray()

    tasks.forEach { task ->

        val jsonObject = JSONObject()

        jsonObject.put("id", task.id)
        jsonObject.put("title", task.title)
        jsonObject.put("description", task.description)
        jsonObject.put("date", task.date)
        jsonObject.put("time", task.time)
        jsonObject.put("category", task.category)
        jsonObject.put("completed", task.completed)

        jsonArray.put(jsonObject)
    }

    val preferences = context.getSharedPreferences(
        "TaskFlowPreferences",
        Context.MODE_PRIVATE
    )

    preferences.edit()
        .putString("tasks", jsonArray.toString())
        .apply()
}

// ======================================================
// CARREGAR TAREFAS
// ======================================================

fun loadTasks(
    context: Context
): List<Task> {

    val preferences = context.getSharedPreferences(
        "TaskFlowPreferences",
        Context.MODE_PRIVATE
    )

    val savedTasks = preferences.getString(
        "tasks",
        "[]"
    ) ?: "[]"

    val taskList = mutableListOf<Task>()

    try {

        val jsonArray = JSONArray(savedTasks)

        for (i in 0 until jsonArray.length()) {

            val jsonObject = jsonArray.getJSONObject(i)

            val task = Task(
                id = jsonObject.optString("id"),
                title = jsonObject.optString("title"),
                description = jsonObject.optString("description"),
                date = jsonObject.optString("date"),
                time = jsonObject.optString("time"),
                category = jsonObject.optString("category"),
                completed = jsonObject.optBoolean("completed")
            )

            taskList.add(task)
        }

    } catch (e: Exception) {

        e.printStackTrace()
    }

    return taskList
}