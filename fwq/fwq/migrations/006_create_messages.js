exports.up = function(knex) {
  return knex.schema.createTable('messages', function(table) {
    table.increments('id').primary();
    table.string('message_id').unique().notNullable(); // UUID
    table.integer('conversation_id').unsigned().notNullable();
    table.integer('sender_id').unsigned();
    table.enum('message_type', ['text', 'image', 'file', 'system']).defaultTo('text');
    table.text('content');
    table.json('metadata'); // 存储额外信息，如文件信息、AI角色信息等
    table.integer('reply_to_id').unsigned(); // 回复消息ID
    table.timestamp('sent_at').defaultTo(knex.fn.now());
    table.boolean('is_deleted').defaultTo(false);
    table.timestamps(true, true);

    table.foreign('conversation_id').references('conversations.id').onDelete('CASCADE');
    table.foreign('sender_id').references('users.id').onDelete('SET NULL');
    table.foreign('reply_to_id').references('messages.id').onDelete('SET NULL');
    
    table.index(['message_id']);
    table.index(['conversation_id', 'sent_at']);
    table.index(['sender_id']);
  });
};

exports.down = function(knex) {
  return knex.schema.dropTable('messages');
};