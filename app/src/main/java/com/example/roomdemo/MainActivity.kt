package com.example.roomdemo

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.example.roomdemo.ui.theme.Pink40
import com.example.roomdemo.ui.theme.Pink80
import com.example.roomdemo.ui.theme.Purple40
import com.example.roomdemo.ui.theme.Purple80
import com.example.roomdemo.ui.theme.PurpleGrey40
import com.example.roomdemo.ui.theme.PurpleGrey80

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun RoomDemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoomDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val owner = LocalViewModelStoreOwner.current
                    owner?.let {
                        val viewModel: MainViewModel = viewModel(
                            it,
                            "MainViewModel",
                            MainViewModelFactory(
                                LocalContext.current.applicationContext as Application
                            )
                        )
                        ScreenSetup(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

// ViewModel Factory класс
class MainViewModelFactory(val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun ScreenSetup(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val allProducts by viewModel.allProducts.observeAsState(listOf())
    val searchResults by viewModel.searchResults.observeAsState(listOf())

    MainScreen(
        modifier = modifier,
        allProducts = allProducts,
        searchResults = searchResults,
        viewModel = viewModel
    )
}

@Composable
fun TitleRow(head1: String, head2: String, head3: String) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Text(
            head1,
            color = Color.White,
            modifier = Modifier.weight(0.1f)
        )
        Text(
            head2,
            color = Color.White,
            modifier = Modifier.weight(0.2f)
        )
        Text(
            head3,
            color = Color.White,
            modifier = Modifier.weight(0.2f)
        )
    }
}

@Composable
fun ProductRow(id: Int, name: String, quantity: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Text(
            id.toString(),
            modifier = Modifier.weight(0.1f)
        )
        Text(
            name,
            modifier = Modifier.weight(0.2f)
        )
        Text(
            quantity.toString(),
            modifier = Modifier.weight(0.2f)
        )
    }
}

@Composable
fun CustomTextField(
    title: String,
    textState: String,
    onTextChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = textState,
        onValueChange = { onTextChange(it) },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        singleLine = true,
        label = { Text(title) },
        modifier = Modifier.padding(10.dp),
        textStyle = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )
    )
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    allProducts: List<Product>,
    searchResults: List<Product>,
    viewModel: MainViewModel
) {
    var productName by remember { mutableStateOf("") }
    var productQuantity by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }

    val onProductTextChange = { text: String ->
        productName = text
    }

    val onQuantityTextChange = { text: String ->
        productQuantity = text
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Поля ввода
        CustomTextField(
            title = "Product Name",
            textState = productName,
            onTextChange = onProductTextChange,
            keyboardType = KeyboardType.Text
        )

        CustomTextField(
            title = "Quantity",
            textState = productQuantity,
            onTextChange = onQuantityTextChange,
            keyboardType = KeyboardType.Number
        )

        // Кнопки действий
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Кнопка Добавить
            Button(onClick = {
                if (productQuantity.isNotEmpty() && productName.isNotEmpty()) {
                    viewModel.insertProduct(
                        Product(
                            productName,
                            productQuantity.toIntOrNull() ?: 0
                        )
                    )
                    searching = false
                    // Очистка полей после добавления
                    productName = ""
                    productQuantity = ""
                }
            }) {
                Text("Add")
            }

            // Кнопка Поиск
            Button(onClick = {
                if (productName.isNotEmpty()) {
                    searching = true
                    viewModel.findProduct(productName)
                }
            }) {
                Text("Search")
            }

            // Кнопка Удалить
            Button(onClick = {
                if (productName.isNotEmpty()) {
                    searching = false
                    viewModel.deleteProduct(productName)
                    // Очистка полей после удаления
                    productName = ""
                    productQuantity = ""
                }
            }) {
                Text("Delete")
            }

            // Кнопка Очистить
            Button(onClick = {
                searching = false
                productName = ""
                productQuantity = ""
            }) {
                Text("Clear")
            }
        }

        // Список продуктов
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            val list = if (searching) searchResults else allProducts

            // Заголовок таблицы
            item {
                TitleRow(head1 = "ID", head2 = "Product", head3 = "Quantity")
            }

            // Список продуктов
            items(list) { product ->
                ProductRow(
                    id = product.id,
                    name = product.productName,
                    quantity = product.quantity
                )
            }
        }
    }
}