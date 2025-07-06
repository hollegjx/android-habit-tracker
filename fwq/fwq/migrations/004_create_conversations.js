exports.up = function(knex) {
  return knex.schema.createTable('conversations', function(table) {
    table.increments('id').primary();
    table.string('conversation_id').unique().notNullable(); // UUID
    table.enum('type', ['private', 'group', 'ai']).defaultTo('private');
    table.string('name'); // 群聊名称
    table.text('description');
    table.integer('created_by').unsigned();
    table.timestamp('last_message_at');
    table.timestamps(true, true);

    table.foreign('created_by').references('users.id').onDelete('SET NULL');
    table.index(['conversation_id']);
    table.index(['type']);
    table.index(['created_by']);
  });
};

exports.down = function(knex) {
  return knex.schema.dropTable('conversations');
};