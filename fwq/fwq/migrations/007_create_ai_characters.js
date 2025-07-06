exports.up = function(knex) {
  return knex.schema.createTable('ai_characters', function(table) {
    table.increments('id').primary();
    table.string('character_id').unique().notNullable(); // UUID
    table.string('name', 100).notNullable();
    table.text('description');
    table.text('personality'); // 角色性格描述
    table.text('system_prompt'); // AI系统提示词
    table.string('avatar_url');
    table.string('model', 50).defaultTo('gpt-3.5-turbo'); // 使用的AI模型
    table.json('model_config'); // 模型配置（temperature等）
    table.boolean('is_active').defaultTo(true);
    table.integer('created_by').unsigned();
    table.timestamps(true, true);

    table.foreign('created_by').references('users.id').onDelete('SET NULL');
    table.index(['character_id']);
    table.index(['is_active']);
    table.index(['created_by']);
  });
};

exports.down = function(knex) {
  return knex.schema.dropTable('ai_characters');
};