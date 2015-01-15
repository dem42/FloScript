-- a script object, which represents executable code (a function for example)
create table scripts (id integer primary key autoincrement,
    name text not null,
    version integer not null,
    created integer not null,
    description text not null,
    code text
);

-- a diagram represents the visualization of a floscript
-- a diagram can be converted to a script and we store the converted script too
create table diagrams (
    id integer primary key autoincrement,
    name text not null,
    version integer not null,
    created integer not null,
    script_id integer,
    foreign key (script_id) references scripts(id)
);

-- an arrow connectable diagram component
create table connectable_diagram_elements (
    id integer primary key autoincrement,
    type text not null,
    x_pos numeric not null,
    y_pos numeric not null,
    pinned integer not null,
    script_id integer,
    diagram_id integer not null,
    foreign key (script_id) references scripts(id),
    foreign key (diagram_id) references diagrams(id)
);

-- connects two components
create table arrows (
    id integer primary key autoincrement,
    source integer not null,
    target integer not null,
    start_x numeric not null,
    start_y numeric not null,
    end_x numeric not null,
    end_y numeric not null,
    diagram_id integer not null,
    foreign key (source) references connectable_diagram_elements(id),
    foreign key (target) references connectable_diagram_elements(id),
    foreign key (diagram_id) references diagrams(id)
);