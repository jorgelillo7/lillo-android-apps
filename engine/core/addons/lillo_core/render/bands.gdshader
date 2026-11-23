// Bands across the ground every `period` metres in two colours, by world z, so speed reads.
shader_type spatial;
render_mode diffuse_toon;
uniform vec4 base : source_color;
uniform vec4 stripe : source_color;
uniform float period = 4.0;
varying vec3 world;
void vertex() { world = (MODEL_MATRIX * vec4(VERTEX, 1.0)).xyz; }
void fragment() { ALBEDO = mod(world.z, period) < period * 0.5 ? base.rgb : stripe.rgb; ROUGHNESS = 0.9; }
