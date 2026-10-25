package com.azharix.collegemanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF07316C)
private val Blue = Color(0xFF0755C9)
private val Pale = Color(0xFFF2F7FD)
private val Gold = Color(0xFFFFC857)
private val Ink = Color(0xFF102B50)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AzharixApp() }
    }
}

@Composable
fun AzharixApp() {
    var loggedIn by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("Dashboard") }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue, secondary = Gold, background = Pale,
            surface = Color.White, onPrimary = Color.White
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Pale) {
            if (!loggedIn) {
                LoginScreen(
                    email = email, onEmail = { email = it },
                    password = password, onPassword = { password = it },
                    onLogin = { loggedIn = true },
                    onGuest = { loggedIn = true }
                )
            } else {
                MainDashboard(selected = selected, onSelect = { selected = it },
                    onLogout = { loggedIn = false; selected = "Dashboard" })
            }
        }
    }
}

@Composable
fun BrandHeader(compact: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = if (compact) 8.dp else 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(color = Navy, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(if (compact) 38.dp else 50.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.School, contentDescription = "AZHARIX logo", tint = Gold,
                    modifier = Modifier.size(if (compact) 25.dp else 34.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text("AZHARIX", fontSize = if (compact) 20.sp else 27.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
            Text("College Management System", fontSize = if (compact) 9.sp else 11.sp, color = Blue)
        }
    }
}

@Composable
fun LoginScreen(
    email: String, onEmail: (String) -> Unit,
    password: String, onPassword: (String) -> Unit,
    onLogin: () -> Unit, onGuest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BrandHeader()
        Spacer(Modifier.height(18.dp))
        Text("Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy)
        Text("Sign in to your college portal", color = Color(0xFF61738B), fontSize = 14.sp)
        Spacer(Modifier.height(26.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(22.dp)) {
                Text("Email or Roll Number", color = Ink, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(
                    value = email, onValueChange = onEmail,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter email or roll number") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(15.dp))
                Text("Password", color = Ink, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(
                    value = password, onValueChange = onPassword,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Login", fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onGuest,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Preview Dashboard") }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Secure • Responsive • Student Focused", color = Color(0xFF61738B), fontSize = 12.sp)
        Text("Starter demo: login is not connected to a real account yet.", color = Color(0xFF8A5A00), fontSize = 11.sp)
    }
}

@Composable
fun MainDashboard(selected: String, onSelect: (String) -> Unit, onLogout: () -> Unit) {
    val items = listOf(
        "Dashboard" to Icons.Default.Dashboard,
        "My Profile" to Icons.Default.Person,
        "Results" to Icons.Default.Assessment,
        "Attendance" to Icons.Default.EventAvailable,
        "Fees" to Icons.Default.AccountBalanceWallet,
        "Time Table" to Icons.Default.CalendarMonth,
        "Notices" to Icons.Default.Campaign,
        "Settings" to Icons.Default.Settings
    )
    Row(Modifier.fillMaxSize()) {
        Column(
            Modifier.width(76.dp).fillMaxHeight().background(Navy).padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.School, null, tint = Gold, modifier = Modifier.size(36.dp))
            Text("AZH", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            items.forEach { (name, icon) ->
                val active = selected == name
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 3.dp)
                        .background(if (active) Blue else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { onSelect(name) }.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(3.dp))
                    Text(name, color = Color.White, fontSize = 8.sp, maxLines = 1)
                }
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, "Logout", tint = Color.White) }
        }

        Column(
            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("AZHARIX", color = Navy, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("College Management System", color = Color.Gray, fontSize = 10.sp)
                }
                Surface(color = Color.White, shape = RoundedCornerShape(50.dp)) {
                    Icon(Icons.Default.Notifications, null, tint = Blue, modifier = Modifier.padding(10.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(if (selected == "Dashboard") "Welcome Back, Student!" else selected,
                color = Navy, fontWeight = FontWeight.Bold, fontSize = 23.sp)
            Text(if (selected == "Dashboard") "Keep going! Your future is bright." else "Your $selected information",
                color = Color(0xFF64748B), fontSize = 12.sp)
            Spacer(Modifier.height(18.dp))

            when (selected) {
                "Dashboard" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard("Attendance", "92%", "Current month", Icons.Default.EventAvailable, Modifier.weight(1f))
                            StatCard("Fees Status", "Paid", "Demo record", Icons.Default.Payments, Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard("Total Marks", "876/1100", "Sample result", Icons.Default.Grade, Modifier.weight(1f))
                            StatCard("Class Rank", "5", "Demo rank", Icons.Default.EmojiEvents, Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    SectionCard("Recent Notices") {
                        NoticeRow("Annual Sports Day", "Event information and schedule")
                        NoticeRow("Mid Term Exam", "Check your examination timetable")
                        NoticeRow("Fee Payment Reminder", "Review fee details with the college office")
                    }
                    Spacer(Modifier.height(12.dp))
                    SectionCard("Quick Access") {
                        items.take(6).chunked(3).forEach { group ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                group.forEach { (label, icon) ->
                                    OutlinedButton(onClick = { onSelect(label) }, modifier = Modifier.weight(1f)) {
                                        Icon(icon, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(label, fontSize = 9.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
                "My Profile" -> SectionCard("Student Profile") {
                    ProfileLine("Student Name", "Student Demo")
                    ProfileLine("Roll Number", "AZ-2026-001")
                    ProfileLine("Class", "To be assigned")
                    ProfileLine("Email", emailForDemo())
                    ProfileLine("Status", "Demo profile")
                }
                "Results" -> SectionCard("Academic Results — Sample Data") {
                    listOf("Mathematics" to "92 / 100", "Physics" to "88 / 100", "Chemistry" to "85 / 100", "English" to "90 / 100").forEach {
                        ProfileLine(it.first, it.second)
                    }
                    Text("These are example marks only, not official college results.", color = Color(0xFF8A5A00), fontSize = 11.sp)
                }
                "Attendance" -> SectionCard("Attendance Summary — Sample Data") {
                    ProfileLine("Present", "20 days")
                    ProfileLine("Absent", "2 days")
                    ProfileLine("Attendance", "92%")
                    Text("Connect a real database before using for official records.", color = Color(0xFF8A5A00), fontSize = 11.sp)
                }
                "Fees" -> SectionCard("Fee Management — Sample Data") {
                    ProfileLine("Tuition Fee", "PKR 15,000")
                    ProfileLine("Library Fee", "PKR 2,000")
                    ProfileLine("Status", "Demo only")
                }
                "Time Table" -> SectionCard("Weekly Timetable") {
                    listOf("Monday — Mathematics", "Tuesday — Physics", "Wednesday — English", "Thursday — Computer Science", "Friday — Chemistry").forEach {
                        NoticeRow(it, "Time to be confirmed by college")
                    }
                }
                "Notices" -> SectionCard("College Notices") {
                    NoticeRow("Welcome to AZHARIX", "Official notices will appear here after backend setup.")
                    NoticeRow("System Setup", "Demo notices are not official announcements.")
                }
                else -> SectionCard("Settings") {
                    ProfileLine("App Version", "1.0 demo")
                    ProfileLine("Account Security", "Backend setup required")
                    ProfileLine("Help", "Contact your college administrator")
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("AZHARIX • College Management System", color = Color.Gray, fontSize = 10.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(12.dp)) {
            Icon(icon, null, tint = Blue, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color(0xFF52647A), fontSize = 10.sp)
            Text(value, color = Navy, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = Color(0xFF77869A), fontSize = 9.sp)
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun NoticeRow(title: String, description: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
        Surface(color = Pale, shape = RoundedCornerShape(8.dp)) {
            Icon(Icons.Default.Campaign, null, tint = Blue, modifier = Modifier.padding(8.dp).size(18.dp))
        }
        Spacer(Modifier.width(9.dp))
        Column {
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(description, color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun ProfileLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
        Text(label, color = Color(0xFF64748B), fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text(value, color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.2f))
    }
    HorizontalDivider(color = Pale)
}

fun emailForDemo() = "student@example.com"
