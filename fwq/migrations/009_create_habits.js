/**
 * 创建习惯相关表
 */

exports.up = function(knex) {
  return knex.schema
    // 创建习惯表
    .createTable('habits', function(table) {
      table.increments('id').primary();
      table.string('habit_id').unique().notNullable(); // UUID
      table.integer('user_id').unsigned().notNullable()
        .references('id').inTable('users').onDelete('CASCADE');
      table.string('name').notNullable();
      table.text('description');
      table.string('category').defaultTo('general');
      table.enum('frequency', ['daily', 'weekly', 'monthly']).defaultTo('daily');
      table.integer('target_value').defaultTo(1);
      table.string('unit').defaultTo('times');
      table.time('reminder_time');
      table.boolean('is_active').defaultTo(true);
      table.json('metadata'); // 存储额外的元数据，如图标、颜色等
      table.timestamps(true, true);
      
      table.index(['user_id', 'is_active']);
      table.index('habit_id');
    })
    
    // 创建习惯完成记录表
    .createTable('habit_completions', function(table) {
      table.increments('id').primary();
      table.string('completion_id').unique().notNullable(); // UUID
      table.integer('habit_id').unsigned().notNullable()
        .references('id').inTable('habits').onDelete('CASCADE');
      table.integer('user_id').unsigned().notNullable()
        .references('id').inTable('users').onDelete('CASCADE');
      table.decimal('value', 10, 2).defaultTo(1); // 完成的数量/程度
      table.text('notes'); // 用户备注
      table.timestamp('completed_at').notNullable();
      table.timestamps(true, true);
      
      table.index(['habit_id', 'completed_at']);
      table.index(['user_id', 'completed_at']);
    })
    
    // 创建习惯统计表（缓存统计数据以提高性能）
    .createTable('habit_statistics', function(table) {
      table.increments('id').primary();
      table.integer('habit_id').unsigned().notNullable()
        .references('id').inTable('habits').onDelete('CASCADE');
      table.integer('user_id').unsigned().notNullable()
        .references('id').inTable('users').onDelete('CASCADE');
      table.date('stat_date').notNullable(); // 统计日期
      table.decimal('completed_value', 10, 2).defaultTo(0); // 当日完成量
      table.boolean('is_completed').defaultTo(false); // 是否达成目标
      table.integer('streak_count').defaultTo(0); // 连续完成天数
      table.timestamps(true, true);
      
      table.unique(['habit_id', 'stat_date']);
      table.index(['user_id', 'stat_date']);
    });
};

exports.down = function(knex) {
  return knex.schema
    .dropTableIfExists('habit_statistics')
    .dropTableIfExists('habit_completions')
    .dropTableIfExists('habits');
};