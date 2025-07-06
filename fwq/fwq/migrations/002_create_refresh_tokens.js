exports.up = function(knex) {
  return knex.schema.createTable('refresh_tokens', function(table) {
    table.increments('id').primary();
    table.integer('user_id').unsigned().notNullable();
    table.string('token').unique().notNullable();
    table.string('device_info');
    table.timestamp('expires_at').notNullable();
    table.timestamps(true, true);

    table.foreign('user_id').references('users.id').onDelete('CASCADE');
    table.index(['user_id']);
    table.index(['token']);
  });
};

exports.down = function(knex) {
  return knex.schema.dropTable('refresh_tokens');
};