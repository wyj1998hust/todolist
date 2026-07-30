import React, { useState, useEffect } from 'react';
import { Plus, Trash2, Calendar, User, CheckCircle2, Clock, AlertCircle } from 'lucide-react';
import { format } from 'date-fns';

function App() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  const [newTask, setNewTask] = useState({
    title: '',
    startTime: format(new Date(), 'yyyy-MM-dd'),
    endTime: format(new Date(), 'yyyy-MM-dd'),
    progress: 0,
    assignee: '',
  });

  const [filter, setFilter] = useState('all');

  const API_URL = `http://${window.location.hostname}:5000/api/tasks`;

  // 从后端获取任务
  const fetchTasks = async () => {
    try {
      const response = await fetch(API_URL);
      const data = await response.json();
      setTasks(data);
    } catch (error) {
      console.error('获取任务失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTasks();
  }, []);

  const addTask = async (e) => {
    e.preventDefault();
    if (!newTask.title.trim()) return;

    const task = {
      ...newTask,
      id: Date.now(),
      status: newTask.progress === 100 ? 'completed' : 'in_progress',
    };

    try {
      const response = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(task),
      });
      if (response.ok) {
        setTasks([task, ...tasks]);
        setNewTask({
          title: '',
          startTime: format(new Date(), 'yyyy-MM-dd'),
          endTime: format(new Date(), 'yyyy-MM-dd'),
          progress: 0,
          assignee: '',
        });
      }
    } catch (error) {
      console.error('添加任务失败:', error);
    }
  };

  const deleteTask = async (id) => {
    try {
      const response = await fetch(`${API_URL}/${id}`, {
        method: 'DELETE',
      });
      if (response.ok) {
        setTasks(tasks.filter(task => task.id !== id));
      }
    } catch (error) {
      console.error('删除任务失败:', error);
    }
  };

  const updateProgress = async (id, progress) => {
    const newProgress = parseInt(progress);
    const newStatus = newProgress === 100 ? 'completed' : 'in_progress';

    try {
      const response = await fetch(`${API_URL}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ progress: newProgress, status: newStatus }),
      });
      if (response.ok) {
        setTasks(tasks.map(task => {
          if (task.id === id) {
            return {
              ...task,
              progress: newProgress,
              status: newStatus
            };
          }
          return task;
        }));
      }
    } catch (error) {
      console.error('更新任务失败:', error);
    }
  };

  const filteredTasks = tasks
    .filter(task => {
      if (filter === 'completed') return task.status === 'completed';
      if (filter === 'in_progress') return task.status === 'in_progress';
      return true;
    })
    .sort((a, b) => a.endTime.localeCompare(b.endTime));

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-2xl shadow-xl overflow-hidden mb-8 border border-gray-100">
          <div className="bg-indigo-600 px-6 py-4">
            <h1 className="text-2xl font-bold text-white flex items-center gap-2">
              <CheckCircle2 className="w-8 h-8" />
              模块任务管理清单
            </h1>
            <p className="text-indigo-100 text-sm mt-1">记录、追踪并共享团队任务进度</p>
          </div>

          <form onSubmit={addTask} className="p-6 grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">任务名称</label>
              <input
                type="text"
                required
                placeholder="输入任务描述..."
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
                value={newTask.title}
                onChange={(e) => setNewTask({ ...newTask, title: e.target.value })}
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">开始时间</label>
              <div className="relative">
                <Calendar className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                <input
                  type="date"
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none"
                  value={newTask.startTime}
                  onChange={(e) => setNewTask({ ...newTask, startTime: e.target.value })}
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">结束时间</label>
              <div className="relative">
                <Calendar className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                <input
                  type="date"
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none"
                  value={newTask.endTime}
                  onChange={(e) => setNewTask({ ...newTask, endTime: e.target.value })}
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">负责人</label>
              <div className="relative">
                <User className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                <input
                  type="text"
                  placeholder="姓名"
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none"
                  value={newTask.assignee}
                  onChange={(e) => setNewTask({ ...newTask, assignee: e.target.value })}
                />
              </div>
            </div>

            <div className="flex items-end">
              <button
                type="submit"
                className="w-full bg-indigo-600 text-white px-6 py-2.5 rounded-lg font-semibold hover:bg-indigo-700 transition-colors flex items-center justify-center gap-2 shadow-md"
              >
                <Plus className="w-5 h-5" />
                添加任务
              </button>
            </div>
          </form>
        </div>

        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            任务列表
            <span className="bg-gray-200 text-gray-600 text-xs px-2 py-1 rounded-full">{filteredTasks.length}</span>
          </h2>
          <div className="flex bg-white rounded-lg p-1 shadow-sm border border-gray-200">
            {['all', 'in_progress', 'completed'].map((f) => (
              <button
                key={f}
                onClick={() => setFilter(f)}
                className={`px-4 py-1.5 rounded-md text-sm font-medium transition-all ${
                  filter === f 
                    ? 'bg-indigo-100 text-indigo-700' 
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                {f === 'all' ? '全部' : f === 'in_progress' ? '进行中' : '已完成'}
              </button>
            ))}
          </div>
        </div>

        <div className="space-y-4">
          {filteredTasks.length === 0 ? (
            <div className="text-center py-12 bg-white rounded-2xl border-2 border-dashed border-gray-200">
              <AlertCircle className="w-12 h-12 text-gray-300 mx-auto mb-3" />
              <p className="text-gray-500">暂无相关任务，开始添加一个吧！</p>
            </div>
          ) : (
            filteredTasks.map((task) => (
              <div key={task.id} className="bg-white rounded-xl shadow-sm border border-gray-100 p-5 hover:shadow-md transition-shadow">
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <h3 className={`text-lg font-bold ${task.status === 'completed' ? 'text-gray-400 line-through' : 'text-gray-800'}`}>
                      {task.title}
                    </h3>
                    <div className="flex flex-wrap gap-4 mt-2 text-sm text-gray-500">
                      <span className="flex items-center gap-1.5">
                        <Clock className="w-4 h-4" />
                        {task.startTime} 至 {task.endTime}
                      </span>
                      <span className="flex items-center gap-1.5 bg-gray-100 px-2 py-0.5 rounded-md">
                        <User className="w-4 h-4" />
                        {task.assignee || '未指派'}
                      </span>
                    </div>
                  </div>
                  <button
                    onClick={() => deleteTask(task.id)}
                    className="text-gray-400 hover:text-red-500 p-1.5 transition-colors"
                  >
                    <Trash2 className="w-5 h-5" />
                  </button>
                </div>

                <div className="flex items-center gap-4">
                  <div className="flex-1">
                    <div className="flex justify-between text-xs font-medium text-gray-500 mb-1.5">
                      <span>进展</span>
                      <span>{task.progress}%</span>
                    </div>
                    <div className="w-full bg-gray-100 rounded-full h-2">
                      <div
                        className={`h-2 rounded-full transition-all duration-500 ${
                          task.progress === 100 ? 'bg-green-500' : 'bg-indigo-500'
                        }`}
                        style={{ width: `${task.progress}%` }}
                      ></div>
                    </div>
                  </div>
                  <div className="w-24">
                    <input
                      type="number"
                      min="0"
                      max="100"
                      value={task.progress}
                      onChange={(e) => updateProgress(task.id, e.target.value)}
                      className="w-full px-2 py-1 text-sm border border-gray-200 rounded-md focus:ring-1 focus:ring-indigo-500 outline-none"
                    />
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default App;
