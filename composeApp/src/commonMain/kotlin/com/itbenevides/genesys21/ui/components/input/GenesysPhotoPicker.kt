package com.itbenevides.genesys21.ui.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itbenevides.genesys21.ui.components.card.GenesysCard
import com.itbenevides.genesys21.ui.components.image.GenesysImage
import com.itbenevides.genesys21.ui.components.text.GenesysFontWeight
import com.itbenevides.genesys21.ui.components.text.GenesysText
import com.itbenevides.genesys21.ui.theme.GenesysDimens

/**
 * Componente de seleção de fotos para o Design System.
 * O botão de adicionar agora faz parte da lista horizontal para garantir visibilidade no Wasm.
 */
@Composable
fun GenesysPhotoPicker(
    urls: List<String>,
    onAddClick: () -> Unit,
    onRemoveClick: (String) -> Unit,
    isUploading: Boolean = false,
    maxPhotos: Int = 5,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(GenesysDimens.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Renderiza as fotos existentes
        items(urls) { url ->
            Box(modifier = Modifier.size(GenesysDimens.PhotoPickerSize)) {
                GenesysImage(
                    url = url,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(GenesysDimens.PhotoPickerCorner),
                )
                IconButton(
                    onClick = { onRemoveClick(url) },
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(GenesysDimens.SpacingSmall)
                            .size(GenesysDimens.IconHuge)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(GenesysDimens.IconSmall),
                    )
                }
            }
        }

        // Renderiza o botão de adicionar se houver espaço
        if (urls.size < maxPhotos) {
            item {
                GenesysCard(
                    modifier = Modifier.size(GenesysDimens.PhotoPickerSize),
                    onClick = onAddClick,
                    shape = RoundedCornerShape(GenesysDimens.PhotoPickerCorner),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(GenesysDimens.IconLarge))
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp),
                                )
                                GenesysText(
                                    text = "Adicionar",
                                    fontWeight = GenesysFontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
