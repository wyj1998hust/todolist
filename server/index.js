const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const mysql = require('mysql2/promise');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 5000;

app.use(cors());
app.use(bodyParser.json());

// TDSQL (MySQL) 数据库连接配置
const dbConfig = {
  host: process.env.DB_HOST || 'localhost',
  port: process.env.DB_PORT || 3306,
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'todolist_db',
};

let pool;

async function initDB() {
  try {
    // 创建数据库连接池
    pool = mysql.createPool(dbConfig);
    
    // 自动创建任务表
    const createTableQuery = `
      CREATE TABLE IF NOT EXISTS tasks (
        id BIGINT PRIMARY KEY,
        title VARCHAR(255) NOT NULL,
        startTime DATE,
        endTime DATE,
        progress INT DEFAULT 0,
        assignee VARCHAR(100),
        status VARCHAR(50) DEFAULT 'in_progress',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `;
    await pool.execute(createTableQuery);
    console.log('TDSQL 数据库初始化成功');
  } catch (err) {
    console.error('TDSQL 数据库连接失败:', err.message);
    console.log('请确保数据库服务已启动并配置了正确的环境变量');
  }
}

initDB();

// 获取所有任务
app.get('/api/tasks', async (req, res) => {
  try {
    const [rows] = await pool.execute('SELECT * FROM tasks ORDER BY created_at DESC');
    // 格式化日期，确保前端显示一致
    const tasks = rows.map(row => ({
      ...row,
      startTime: row.startTime ? row.startTime.toISOString().split('T')[0] : null,
      endTime: row.endTime ? row.endTime.toISOString().split('T')[0] : null
    }));
    res.json(tasks);
  } catch (err) {
    res.status(500).json({ error: '无法读取任务' });
  }
});

// 添加任务
app.post('/api/tasks', async (req, res) => {
  try {
    const { id, title, startTime, endTime, progress, assignee, status } = req.body;
    const query = 'INSERT INTO tasks (id, title, startTime, endTime, progress, assignee, status) VALUES (?, ?, ?, ?, ?, ?, ?)';
    await pool.execute(query, [id, title, startTime, endTime, progress, assignee, status]);
    res.status(201).json(req.body);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: '无法添加任务' });
  }
});

// 更新任务进度
app.put('/api/tasks/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { progress, status } = req.body;
    const query = 'UPDATE tasks SET progress = ?, status = ? WHERE id = ?';
    await pool.execute(query, [progress, status, id]);
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: '无法更新任务' });
  }
});

// 删除任务
app.delete('/api/tasks/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const query = 'DELETE FROM tasks WHERE id = ?';
    await pool.execute(query, [id]);
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: '无法删除任务' });
  }
});

app.listen(PORT, () => {
  console.log(`后端服务器正在运行在 http://localhost:${PORT}`);
});
