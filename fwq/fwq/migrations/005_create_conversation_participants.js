exports.up = function(knex) {
  return knex.schema.createTable('conversation_participants', function(table) {
    table.increments('id').primary();
    table.integer('conversation_id').unsigned().notNullable();
    table.integer('user_id').unsigned().notNullable();
    table.enum('role', ['member', 'admin', 'owner']).defaultTo('member');
    table.timestamp('joined_at').defaultTo(knex.fn.now());
    table.timestamp('last_read_at');
    table.timestamps(true, true);

    table.foreign('conversation_id').references('conversations.id').onDelete('CASCADE');
    table.foreign('user_id').references('users.id').onDelete('CASCADE');
    
    table.unique(['conversation_id', 'user_id']);
    table.index(['conversation_id']);
    table.index(['user_id']);
  });
};

exports.down = function(knex) {
  return knex.schema.dropTable('conversation_participants');
};