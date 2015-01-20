-- a script object, which represents executable code (a function for example)
create table scripts (
    _id integer primary key autoincrement,
    name text not null,
    version integer not null,
    created integer not null,
    description text,
    diagram_name text,
    diagram_version int,
    code text
);

-- a diagram represents the visualization of a floscript
-- a diagram can be converted to a script and we store the converted script too
create table diagrams (
    _id integer primary key autoincrement,
    name text not null,
    version integer not null,
    created integer not null,
    description text,
    script_id integer,
    foreign key (script_id) references scripts(_id)
);

-- an arrow connectable diagram component
create table connectable_diagram_elements (
    _id integer primary key autoincrement,
    type text not null,
    x_pos numeric not null,
    y_pos numeric not null,
    pinned integer not null,
    script_id integer,
    diagram_id integer not null,
    foreign key (script_id) references scripts(_id),
    foreign key (diagram_id) references diagrams(_id)
);

-- connects two components
create table arrows (
    _id integer primary key autoincrement,
    source integer not null,
    target integer not null,
    condition integer,
    diagram_id integer not null,
    foreign key (source) references connectable_diagram_elements(_id),
    foreign key (target) references connectable_diagram_elements(_id),
    foreign key (diagram_id) references diagrams(_id)
);

-- jobs for the job execution service to execute
create table jobs (
    _id integer primary key autoincrement,
    name text not null,
    script_id integer not null,
    created integer not null,
    enabled integer not null,
    time_trigger integer,
    event_trigger integer,
    comments text,
    foreign key (script_id) references scripts(_id)
);
