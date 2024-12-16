package yixin16.ch.com.example.snakegame

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.delay
import kotlin.random.Random


enum class Direction { UP, DOWN, LEFT, RIGHT }

data class SnakePart(val x: Int, val y: Int)

@Composable
fun SnakeGame() {
    // Canvas size (e.g., 20x20 grid)
    val gridSize = 20
    val boxSize = 20.dp

    // State for snake, reward, direction, and game over
    var snake by remember {
        mutableStateOf(
            listOf(
                SnakePart(10, 10),
                SnakePart(10, 11),
                SnakePart(10, 12),
                SnakePart(10, 13)
            )
        )
    }
    var direction by remember { mutableStateOf(Direction.RIGHT) }
    var reward by remember {
        mutableStateOf(
            SnakePart(
                Random.nextInt(0, gridSize),
                Random.nextInt(0, gridSize)
            )
        )
    }

    // State to control whether the snake just ate the reward
    var justAteReward by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(200L)  //decide speed, less for more speed
            snake = moveSnake(snake, direction, justAteReward)
            justAteReward = false // Reset the flag after moving the snake

            // Check for collision with reward (including wrapping behavior)
            val head = snake.first()
            val wrappedHead = wrapPosition(head, gridSize) // Ensure head is wrapped before checking
            if (wrappedHead == reward) {
                justAteReward = true // Grow the snake
                reward = generateNewReward(snake, gridSize)  //generate new reward
            }

            // Check if snake has collided with itself
            if (snake.drop(1).contains(wrappedHead)) {
                // Reduce snake size from the point of collision
                snake = snake.dropWhile { it != wrappedHead }.drop(1)
            }

            // Wrap the snake within the grid boundaries
            snake = snake.map { wrapPosition(it, gridSize) }
        }
    }

    // UI for game
    Column(
        modifier = Modifier.fillMaxSize().padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Snake game canvas
        Canvas(modifier = Modifier.size(gridSize * boxSize).background(Color.LightGray.copy(0.3f))) {
            // Draw snake
            snake.forEach { part ->
                drawRoundRect(
                    Color.Green,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        part.x * boxSize.toPx(),
                        part.y * boxSize.toPx()
                    ),
                    size = androidx.compose.ui.geometry.Size(boxSize.toPx(), boxSize.toPx()),
                    cornerRadius = CornerRadius(10f, 10f)
                )
            }

            // Draw reward
            drawRoundRect(
                Color.Red,
                topLeft = androidx.compose.ui.geometry.Offset(
                    reward.x * boxSize.toPx(),
                    reward.y * boxSize.toPx()
                ),
                size = androidx.compose.ui.geometry.Size(boxSize.toPx(), boxSize.toPx()),
                cornerRadius = CornerRadius(10f, 10f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { if (direction != Direction.DOWN) direction = Direction.UP },
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = ""
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { if (direction != Direction.RIGHT) direction = Direction.LEFT },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = ""
                )
            }

            Button(
                onClick = { if (direction != Direction.LEFT) direction = Direction.RIGHT },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = ""
                )
            }
        }

        Button(
            onClick = { if (direction != Direction.UP) direction = Direction.DOWN },
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = ""
            )
        }
    }
}

// Function to move the snake
fun moveSnake(snake: List<SnakePart>, direction: Direction, grow: Boolean): List<SnakePart> {
    val head = snake.first()
    val newHead = when (direction) {
        Direction.UP -> SnakePart(head.x, head.y - 1)
        Direction.DOWN -> SnakePart(head.x, head.y + 1)
        Direction.LEFT -> SnakePart(head.x - 1, head.y)
        Direction.RIGHT -> SnakePart(head.x + 1, head.y)
    }
    return if (grow) {
        listOf(newHead) + snake // Add new head and keep the entire body (grow)
    } else {
        listOf(newHead) + snake.dropLast(1) // Move by adding new head and dropping last part
    }
}

// Function to wrap a single position within the grid boundaries
fun wrapPosition(part: SnakePart, gridSize: Int): SnakePart {
    return SnakePart(
        x = (part.x + gridSize) % gridSize,  // if snake @ (20, 10), so (20 + 20) % 20 = 40 % 20 = 0 -->snake reappears at the leftmost position of the grid (0, 10)
        y = (part.y + gridSize) % gridSize  // if snake @ (5, -1), so (-1 + 20) % 20 = 19 % 20 = 19 -->snake reappears at the bottom of the grid (5, 19)
    )
}

// Function to generate a new reward that doesn't overlap with the snake
fun generateNewReward(snake: List<SnakePart>, gridSize: Int): SnakePart {
    var newReward: SnakePart
    do {
        newReward = SnakePart(Random.nextInt(0, gridSize), Random.nextInt(0, gridSize))
    } while (snake.contains(newReward))
    return newReward
}