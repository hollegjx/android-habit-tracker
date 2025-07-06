exports.up = function(knex) {
  return knex.schema.createTable('verification_codes', function(table) {
    table.increments('id').primary();
    table.string('target').notNullable(); // 邮箱或手机号
    table.enum('type', ['email', 'phone']).notNullable();
    table.enum('purpose', ['register', 'reset', 'login']).notNullable();
    table.string('code', 10).notNullable();
    table.timestamp('expires_at').notNullable();
    table.boolean('is_used').defaultTo(false);
    table.timestamps(true, true);

    table.index(['target', 'type', 'purpose']);
    table.index(['code']);
    table.index(['expires_at']);
  });
};

exports.down = function(knex) {
  return knex.schema.dropTable('verification_codes');
};