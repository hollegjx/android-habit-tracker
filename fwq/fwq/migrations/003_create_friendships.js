exports.up = function(knex) {
  return knex.schema.createTable('friendships', function(table) {
    table.increments('id').primary();
    table.integer('requester_id').unsigned().notNullable();
    table.integer('addressee_id').unsigned().notNullable();
    table.enum('status', ['pending', 'accepted', 'declined', 'blocked']).defaultTo('pending');
    table.timestamps(true, true);

    table.foreign('requester_id').references('users.id').onDelete('CASCADE');
    table.foreign('addressee_id').references('users.id').onDelete('CASCADE');
    
    // 确保不能重复添加好友
    table.unique(['requester_id', 'addressee_id']);
    table.index(['requester_id']);
    table.index(['addressee_id']);
    table.index(['status']);
  });
};

exports.down = function(knex) {
  return knex.schema.dropTable('friendships');
};