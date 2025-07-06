const fs = require('fs');
const path = require('path');

/**
 * 自定义日志管理器
 * 支持文件日志和内存日志，便于管理员后台查看
 */
class Logger {
  constructor() {
    this.logs = []; // 内存中保存最近的日志
    this.maxMemoryLogs = 1000; // 最多保存1000条日志在内存中
    this.logDir = path.join(__dirname, '../../logs');
    this.logFile = path.join(this.logDir, `app-${new Date().toISOString().split('T')[0]}.log`);
    
    // 确保日志目录存在
    this.ensureLogDir();
    
    // 初始化日志文件
    this.initLogFile();
  }

  ensureLogDir() {
    if (!fs.existsSync(this.logDir)) {
      fs.mkdirSync(this.logDir, { recursive: true });
    }
  }

  initLogFile() {
    const startupLog = this.formatLog('INFO', 'SYSTEM', '服务器启动', { timestamp: new Date().toISOString() });
    this.writeToFile(startupLog);
  }

  formatLog(level, category, message, data = null) {
    const timestamp = new Date().toISOString();
    const logEntry = {
      timestamp,
      level,
      category,
      message,
      data,
      id: Date.now() + Math.random()
    };

    return logEntry;
  }

  writeToFile(logEntry) {
    try {
      const logLine = `${logEntry.timestamp} [${logEntry.level}] [${logEntry.category}] ${logEntry.message}${logEntry.data ? ' | Data: ' + JSON.stringify(logEntry.data) : ''}\n`;
      fs.appendFileSync(this.logFile, logLine);
    } catch (error) {
      console.error('写入日志文件失败:', error);
    }
  }

  addToMemory(logEntry) {
    this.logs.push(logEntry);
    // 保持内存日志数量限制
    if (this.logs.length > this.maxMemoryLogs) {
      this.logs.shift(); // 移除最旧的日志
    }
  }

  log(level, category, message, data = null) {
    const logEntry = this.formatLog(level, category, message, data);
    
    // 添加到内存
    this.addToMemory(logEntry);
    
    // 写入文件
    this.writeToFile(logEntry);
    
    // 同时输出到控制台
    const consoleMessage = `[${level}] [${category}] ${message}`;
    switch (level) {
      case 'ERROR':
        console.error(consoleMessage, data || '');
        break;
      case 'WARN':
        console.warn(consoleMessage, data || '');
        break;
      case 'INFO':
        console.info(consoleMessage, data || '');
        break;
      case 'DEBUG':
        console.log(consoleMessage, data || '');
        break;
      default:
        console.log(consoleMessage, data || '');
    }
  }

  // 便捷方法
  info(category, message, data = null) {
    this.log('INFO', category, message, data);
  }

  warn(category, message, data = null) {
    this.log('WARN', category, message, data);
  }

  error(category, message, data = null) {
    this.log('ERROR', category, message, data);
  }

  debug(category, message, data = null) {
    this.log('DEBUG', category, message, data);
  }

  // 获取内存中的日志
  getRecentLogs(limit = 100) {
    return this.logs.slice(-limit).reverse(); // 返回最新的日志，倒序排列
  }

  // 根据级别过滤日志
  getLogsByLevel(level, limit = 100) {
    return this.logs
      .filter(log => log.level === level)
      .slice(-limit)
      .reverse();
  }

  // 根据分类过滤日志
  getLogsByCategory(category, limit = 100) {
    return this.logs
      .filter(log => log.category === category)
      .slice(-limit)
      .reverse();
  }

  // 搜索日志
  searchLogs(query, limit = 100) {
    const queryLower = query.toLowerCase();
    return this.logs
      .filter(log => 
        log.message.toLowerCase().includes(queryLower) ||
        log.category.toLowerCase().includes(queryLower) ||
        (log.data && JSON.stringify(log.data).toLowerCase().includes(queryLower))
      )
      .slice(-limit)
      .reverse();
  }

  // 清空内存日志（保留文件日志）
  clearMemoryLogs() {
    this.logs = [];
    this.info('SYSTEM', '内存日志已清空');
  }

  // 获取日志统计
  getLogStats() {
    const stats = {
      total: this.logs.length,
      byLevel: {},
      byCategory: {},
      recentErrors: 0,
      recentWarnings: 0
    };

    // 统计最近1小时的错误和警告
    const oneHourAgo = new Date(Date.now() - 60 * 60 * 1000);

    this.logs.forEach(log => {
      // 按级别统计
      stats.byLevel[log.level] = (stats.byLevel[log.level] || 0) + 1;
      
      // 按分类统计
      stats.byCategory[log.category] = (stats.byCategory[log.category] || 0) + 1;
      
      // 统计最近错误和警告
      const logTime = new Date(log.timestamp);
      if (logTime > oneHourAgo) {
        if (log.level === 'ERROR') stats.recentErrors++;
        if (log.level === 'WARN') stats.recentWarnings++;
      }
    });

    return stats;
  }
}

// 创建全局日志实例
const logger = new Logger();

module.exports = logger;